export const ideaContestAuthKey = 'zdoIdeaContestAuthUserId'
export const ideaContestAuthTokenKey = 'zdoIdeaContestAuthToken'
export const postLoginRedirectKey = 'zdoPostLoginRedirectPath'

export function saveIdeaContestLogin(userId) {
  if (!userId) return
  try {
    localStorage.setItem(ideaContestAuthKey, String(userId))
  } catch (_) {}
}

export function isIdeaContestLoggedIn() {
  try {
    const userId = localStorage.getItem(ideaContestAuthKey)
    return Boolean(userId)
  } catch (_) {
    return false
  }
}

export function saveIdeaContestAuthToken(token) {
  if (!token) return
  try {
    localStorage.setItem(ideaContestAuthTokenKey, token)
  } catch (_) {}
}

export function getIdeaContestAuthToken() {
  try {
    return localStorage.getItem(ideaContestAuthTokenKey) || ''
  } catch (_) {
    return ''
  }
}

export function setGlobalRedirectPath(pathname) {
  if (!pathname || !pathname.startsWith('/')) return
  try {
    localStorage.setItem(postLoginRedirectKey, pathname)
  } catch (_) {}
}

export function clearGlobalRedirectPath() {
  try {
    localStorage.removeItem(postLoginRedirectKey)
  } catch (_) {}
}

export function getGlobalRedirectPath() {
  try {
    const saved = localStorage.getItem(postLoginRedirectKey)
    return saved && saved.startsWith('/') ? saved : ''
  } catch (_) {
    return ''
  }
}
