const KEY = 'insightflow.session'

export function clearSession() {
  localStorage.removeItem(KEY)
  sessionStorage.removeItem(KEY)
}

export function getSession() {
  try {
    const session = JSON.parse(sessionStorage.getItem(KEY) || localStorage.getItem(KEY) || 'null')
    if (session?.token && Date.parse(session.expiresAt) > Date.now()) return session
  } catch { /* Uma sessao invalida exige novo login. */ }
  clearSession()
  return null
}

export function saveSession(session, remember) {
  clearSession()
  ;(remember ? localStorage : sessionStorage).setItem(KEY, JSON.stringify(session))
}
