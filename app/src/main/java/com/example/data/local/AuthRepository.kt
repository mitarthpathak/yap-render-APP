package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Local, offline-only account store for the Yap & Render prototype.
 *
 * There is no backend in this build. Accounts, preferences, learning stats and
 * translation history all live in a single [SharedPreferences] file. Passwords
 * are stored as a per-user salt + SHA-256 hash, never in plain text.
 *
 * A production release would swap this for Firebase Auth + Credential Manager —
 * the dependencies are already declared (commented) in `app/build.gradle.kts`.
 */
class AuthRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---------------------------------------------------------------- Models

    data class Account(
        val name: String,
        val email: String,
        val role: String,
        val organization: String,
        val goal: String,
        val joinedAtMillis: Long,
        val isGuest: Boolean = false,
        val avatarType: String = "DEFAULT",
        val speed: Float = 1.0f
    ) {
        val initial: String get() = name.trim().take(1).uppercase().ifBlank { "Y" }
    }

    data class Stats(
        val phrasesTranslated: Int = 0,
        val wordsSigned: Int = 0,
        val lettersFingerspelled: Int = 0,
        val dayStreak: Int = 0
    )

    data class HistoryEntry(val phrase: String, val timestamp: Long)

    sealed interface AuthResult {
        data object Success : AuthResult
        data class Failure(val message: String) : AuthResult
    }

    // --------------------------------------------------------------- Session

    val isSignedIn: Boolean get() = session() != null

    private fun session(): String? = prefs.getString(KEY_SESSION, null)

    fun currentAccount(): Account? {
        val key = session() ?: return null
        return readAccount(key)
    }

    fun hasSeenOnboarding(): Boolean = prefs.getBoolean(KEY_ONBOARDING_SEEN, false)

    fun setOnboardingSeen() = prefs.edit().putBoolean(KEY_ONBOARDING_SEEN, true).apply()

    // ------------------------------------------------------------ Sign up/in

    fun signUp(
        name: String,
        email: String,
        password: String,
        role: String,
        organization: String,
        goal: String
    ): AuthResult {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()

        if (cleanName.length < 2) return AuthResult.Failure("Enter your full name.")
        if (!isEmailValid(cleanEmail)) return AuthResult.Failure("That email address looks off.")
        if (password.length < 6) return AuthResult.Failure("Password needs at least 6 characters.")
        if (accountKeys().contains(cleanEmail)) {
            return AuthResult.Failure("An account with this email already exists.")
        }

        val salt = newSalt()
        val record = JSONObject().apply {
            put(F_NAME, cleanName)
            put(F_EMAIL, cleanEmail)
            put(F_ROLE, role)
            put(F_ORG, organization.trim())
            put(F_GOAL, goal)
            put(F_JOINED, System.currentTimeMillis())
            put(F_SALT, salt)
            put(F_HASH, hash(password, salt))
            put(F_AVATAR, "DEFAULT")
            put(F_SPEED, 1.0.toString())
        }
        writeAccountJson(cleanEmail, record)
        registerAccountKey(cleanEmail)
        prefs.edit().putString(KEY_SESSION, cleanEmail).apply()
        touchStreak(cleanEmail)
        return AuthResult.Success
    }

    fun signIn(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        if (!isEmailValid(cleanEmail)) return AuthResult.Failure("That email address looks off.")

        val record = readAccountJson(cleanEmail)
            ?: return AuthResult.Failure("No account found for this email.")
        val salt = record.optString(F_SALT)
        if (hash(password, salt) != record.optString(F_HASH)) {
            return AuthResult.Failure("Incorrect password. Try again.")
        }
        prefs.edit().putString(KEY_SESSION, cleanEmail).apply()
        touchStreak(cleanEmail)
        return AuthResult.Success
    }

    fun continueAsGuest() {
        if (readAccountJson(GUEST_KEY) == null) {
            val record = JSONObject().apply {
                put(F_NAME, "Guest")
                put(F_EMAIL, "")
                put(F_ROLE, "Explorer")
                put(F_ORG, "")
                put(F_GOAL, "Just exploring")
                put(F_JOINED, System.currentTimeMillis())
                put(F_GUEST, true)
                put(F_AVATAR, "DEFAULT")
                put(F_SPEED, 1.0.toString())
            }
            writeAccountJson(GUEST_KEY, record)
        }
        prefs.edit().putString(KEY_SESSION, GUEST_KEY).apply()
        touchStreak(GUEST_KEY)
    }

    fun signOut() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    // ---------------------------------------------------------- Profile edit

    fun updateProfile(name: String, role: String, organization: String, goal: String): AuthResult {
        val key = session() ?: return AuthResult.Failure("Not signed in.")
        val cleanName = name.trim()
        if (cleanName.length < 2) return AuthResult.Failure("Enter your full name.")
        val record = readAccountJson(key) ?: return AuthResult.Failure("Account missing.")
        record.put(F_NAME, cleanName)
        record.put(F_ROLE, role)
        record.put(F_ORG, organization.trim())
        record.put(F_GOAL, goal)
        record.put(F_GUEST, false) // claiming a profile graduates a guest
        writeAccountJson(key, record)
        return AuthResult.Success
    }

    fun savePreferences(avatarType: String, speed: Float) {
        val key = session() ?: return
        val record = readAccountJson(key) ?: return
        record.put(F_AVATAR, avatarType)
        record.put(F_SPEED, speed.toString())
        writeAccountJson(key, record)
    }

    // ------------------------------------------------------------ Stats/history

    fun stats(): Stats {
        val key = session() ?: return Stats()
        val s = prefs.getString(statsKey(key), null)?.let { JSONObject(it) } ?: return Stats()
        return Stats(
            phrasesTranslated = s.optInt(S_PHRASES),
            wordsSigned = s.optInt(S_WORDS),
            lettersFingerspelled = s.optInt(S_LETTERS),
            dayStreak = s.optInt(S_STREAK)
        )
    }

    fun history(): List<HistoryEntry> {
        val key = session() ?: return emptyList()
        val arr = prefs.getString(historyKey(key), null)?.let { JSONArray(it) } ?: return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(HistoryEntry(o.optString("p"), o.optLong("t")))
            }
        }
    }

    fun clearHistory() {
        val key = session() ?: return
        prefs.edit().remove(historyKey(key)).apply()
    }

    /** Called whenever the avatar plays a phrase. Updates counters + recent list. */
    fun recordTranslation(rawPhrase: String) {
        val key = session() ?: return
        val phrase = rawPhrase.trim()
        if (phrase.isBlank()) return

        val tokens = phrase.split("\\s+".toRegex()).filter { it.isNotBlank() }
        var addWords = 0
        var addLetters = 0
        for (t in tokens) {
            if (KNOWN_WORDS.contains(t.uppercase())) addWords++ else addLetters += t.count { it.isLetter() }
        }

        val current = prefs.getString(statsKey(key), null)?.let { JSONObject(it) } ?: JSONObject()
        current.put(S_PHRASES, current.optInt(S_PHRASES) + 1)
        current.put(S_WORDS, current.optInt(S_WORDS) + addWords)
        current.put(S_LETTERS, current.optInt(S_LETTERS) + addLetters)
        // streak field is owned by touchStreak(); preserve it
        prefs.edit().putString(statsKey(key), current.toString()).apply()

        val arr = prefs.getString(historyKey(key), null)?.let { JSONArray(it) } ?: JSONArray()
        val trimmed = JSONArray()
        trimmed.put(JSONObject().put("p", phrase).put("t", System.currentTimeMillis()))
        var added = 1
        for (i in 0 until arr.length()) {
            if (added >= MAX_HISTORY) break
            val o = arr.getJSONObject(i)
            if (o.optString("p").equals(phrase, ignoreCase = true)) continue // de-dupe
            trimmed.put(o)
            added++
        }
        prefs.edit().putString(historyKey(key), trimmed.toString()).apply()
    }

    // ------------------------------------------------------------- Internals

    private fun touchStreak(key: String) {
        val today = System.currentTimeMillis() / DAY_MS
        val stats = prefs.getString(statsKey(key), null)?.let { JSONObject(it) } ?: JSONObject()
        val last = stats.optLong(S_LAST_DAY, 0L)
        val streak = stats.optInt(S_STREAK, 0)
        val next = when (last) {
            today -> streak.coerceAtLeast(1)
            today - 1 -> streak + 1
            else -> 1
        }
        stats.put(S_STREAK, next)
        stats.put(S_LAST_DAY, today)
        prefs.edit().putString(statsKey(key), stats.toString()).apply()
    }

    private fun readAccount(key: String): Account? {
        val o = readAccountJson(key) ?: return null
        return Account(
            name = o.optString(F_NAME, "You"),
            email = o.optString(F_EMAIL, ""),
            role = o.optString(F_ROLE, "Explorer"),
            organization = o.optString(F_ORG, ""),
            goal = o.optString(F_GOAL, ""),
            joinedAtMillis = o.optLong(F_JOINED, System.currentTimeMillis()),
            isGuest = o.optBoolean(F_GUEST, false),
            avatarType = o.optString(F_AVATAR, "DEFAULT"),
            speed = o.optString(F_SPEED, "1.0").toFloatOrNull() ?: 1.0f
        )
    }

    private fun readAccountJson(key: String): JSONObject? =
        prefs.getString(accountKey(key), null)?.let { runCatching { JSONObject(it) }.getOrNull() }

    private fun writeAccountJson(key: String, obj: JSONObject) =
        prefs.edit().putString(accountKey(key), obj.toString()).apply()

    private fun accountKeys(): Set<String> =
        prefs.getStringSet(KEY_ACCOUNT_INDEX, emptySet()) ?: emptySet()

    private fun registerAccountKey(email: String) {
        val next = accountKeys().toMutableSet().apply { add(email) }
        prefs.edit().putStringSet(KEY_ACCOUNT_INDEX, next).apply()
    }

    private fun accountKey(key: String) = "acct_$key"
    private fun statsKey(key: String) = "stats_$key"
    private fun historyKey(key: String) = "hist_$key"

    private fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hash(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest((salt + password).toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    private fun isEmailValid(email: String): Boolean =
        email.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))

    companion object {
        private const val PREFS_NAME = "yap_auth"
        private const val KEY_SESSION = "session_key"
        private const val KEY_ACCOUNT_INDEX = "account_index"
        private const val KEY_ONBOARDING_SEEN = "onboarding_seen"
        private const val GUEST_KEY = "__guest__"

        private const val F_NAME = "name"
        private const val F_EMAIL = "email"
        private const val F_ROLE = "role"
        private const val F_ORG = "org"
        private const val F_GOAL = "goal"
        private const val F_JOINED = "joined"
        private const val F_GUEST = "guest"
        private const val F_SALT = "salt"
        private const val F_HASH = "hash"
        private const val F_AVATAR = "avatar"
        private const val F_SPEED = "speed"

        private const val S_PHRASES = "phrases"
        private const val S_WORDS = "words"
        private const val S_LETTERS = "letters"
        private const val S_STREAK = "streak"
        private const val S_LAST_DAY = "lastDay"

        private const val MAX_HISTORY = 24
        private const val DAY_MS = 86_400_000L

        /** Words the avatar has real gesture keyframes for (rest is fingerspelled). */
        private val KNOWN_WORDS = setOf(
            "YOU", "HOME", "TIME", "PERSON", "HELLO", "HELP", "THANK", "THANKS",
            "YES", "NO", "PLEASE", "FRIEND", "LOVE", "EAT", "DRINK", "FOOD", "WATER",
            "SORRY", "WELCOME", "GOOD", "BAD", "STOP", "WAIT", "COME", "GO", "WANT",
            "NEED", "LIKE", "KNOW", "UNDERSTAND", "ASK", "DOCTOR", "HOSPITAL", "SCHOOL"
        )

        @Volatile
        private var instance: AuthRepository? = null

        fun get(context: Context): AuthRepository =
            instance ?: synchronized(this) {
                instance ?: AuthRepository(context).also { instance = it }
            }
    }
}
