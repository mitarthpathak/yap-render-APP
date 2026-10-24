// Bundles the web app's code (web/src) for the Android WebViews:
//   app/src/main/assets/avatar/avatar_bundle.js   3D ISL avatar (Avatar3DWebView.kt)
//   app/src/main/assets/sign/                     camera Sign → Text app (SignCameraWebView.kt)
import { build } from 'esbuild'
import { copyFile, mkdir, readFile, rm } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const here = path.dirname(fileURLToPath(import.meta.url))
const src = path.join(here, 'src')
const assets = path.join(here, '..', 'app', 'src', 'main', 'assets')
const signOut = path.join(assets, 'sign')
const target = ['chrome96']

/** The Next.js-only pieces the web code relies on, mapped for a static bundle. */
const webApp = {
  name: 'web-app',
  setup(b) {
    b.onResolve({ filter: /^@\// }, async (args) => {
      const result = await b.resolve('./' + args.path.slice(2), { kind: args.kind, resolveDir: src })
      return result.errors.length ? { errors: result.errors } : { path: result.path }
    })
    b.onResolve({ filter: /^next\/link$/ }, () => ({ path: path.join(here, 'mobile', 'next-link.tsx') }))
    // Tailwind directives: the camera pages use none of its utilities.
    b.onLoad({ filter: /[\\/]app[\\/]globals\.css$/ }, async (args) => {
      const css = (await readFile(args.path, 'utf8'))
        .replace(/^@import '(tailwindcss|tw-animate-css|shadcn\/tailwind\.css)';\n/gm, '')
        .replace(/^@custom-variant[^\n]*\n/gm, '')
        .replace(/^@theme inline \{[\s\S]*?\n\}\n/m, '')
      return { contents: css, loader: 'css', resolveDir: path.dirname(args.path) }
    })
    // Workers are built as separate files in assets/sign/. The URL is absolute
    // because the code that creates them ends up in a chunk under chunks/.
    b.onLoad({ filter: /[\\/]lib[\\/]sign[\\/](signer-tracker|word-recognizer)\.ts$/ }, async (args) => ({
      contents: (await readFile(args.path, 'utf8')).replace(/new URL\('\.\/([\w-]+)\.worker\.ts', import\.meta\.url\)/g, "new URL('/assets/sign/$1.worker.js', import.meta.url)"),
      loader: 'ts',
    }))
  },
}

const common = {
  bundle: true, minify: true, target, legalComments: 'none', logLevel: 'info',
  plugins: [webApp], define: { 'process.env.NODE_ENV': '"production"' },
}

await build({
  ...common,
  entryPoints: [path.join(here, 'mobile', 'avatar-entry.ts')],
  outfile: path.join(assets, 'avatar', 'avatar_bundle.js'),
  format: 'esm',
})

for (const dir of ['chunks', 'fonts']) await rm(path.join(signOut, dir), { recursive: true, force: true })
await build({
  ...common,
  entryPoints: [path.join(here, 'mobile', 'sign-app.tsx')],
  outdir: signOut,
  format: 'esm', splitting: true, jsx: 'automatic',
  chunkNames: 'chunks/[name]-[hash]', assetNames: 'fonts/[name]-[hash]', publicPath: '/assets/sign',
  loader: { '.woff2': 'file', '.woff': 'file' },
})

await build({
  ...common,
  entryPoints: ['signer-tracker.worker', 'word-matcher.worker'].map((name) => path.join(src, 'lib', 'sign', `${name}.ts`)),
  outdir: signOut,
  format: 'iife',
})

await mkdir(signOut, { recursive: true })
await copyFile(path.join(here, 'mobile', 'sign.html'), path.join(signOut, 'index.html'))
