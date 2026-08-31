package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TopNavBar(
    isLoggedIn: Boolean = true,
    username: String = "Mitarth",
    onAccountClick: () -> Unit,
    onLogoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Brand Name using the custom Yap & Render Vector Identity
        YapRenderBrandLogo(
            iconSize = 38.dp,
            showTagline = true,
            onClick = onLogoClick,
            modifier = Modifier.testTag("app_brand_logo")
        )

        // Top Right Corner: ONLY Log in or Username (e.g. Mitarth)
        if (isLoggedIn) {
            // Logged-in Username Pill
            Row(
                modifier = Modifier
                    .tapBounce()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, YapBorderColor, RoundedCornerShape(20.dp))
                    .clickable { onAccountClick() }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("nav_username_btn"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(AvatarTealBase),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.take(1).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = username,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = YapHeadingBlack
                )
            }
        } else {
            // Log In Button
            Button(
                onClick = { onAccountClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = YapNavyDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.testTag("nav_login_btn")
            ) {
                Text(
                    text = "Log In",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
