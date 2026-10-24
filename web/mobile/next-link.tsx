// Stand-in for `next/link` inside the Android WebView. The camera pages are
// served as one bundle, so every link is an ordinary navigation that the app's
// asset loader answers with sign.html. "/" (the web home page) belongs to the
// native app, so it closes the camera screen instead.
import { forwardRef, type AnchorHTMLAttributes, type MouseEvent } from 'react'
import { closeSignScreen } from './android-bridge'

type Props = AnchorHTMLAttributes<HTMLAnchorElement> & { href: string; prefetch?: boolean; replace?: boolean; scroll?: boolean }

const Link = forwardRef<HTMLAnchorElement, Props>(function Link({ href, prefetch, replace, scroll, onClick, ...rest }, ref) {
  void prefetch; void scroll
  const navigate = (event: MouseEvent<HTMLAnchorElement>) => {
    onClick?.(event)
    if (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return
    event.preventDefault()
    if (href === '/' || href === '') { closeSignScreen(); return }
    if (replace) location.replace(href)
    else location.assign(href)
  }
  return <a ref={ref} href={href} onClick={navigate} {...rest} />
})

export default Link
