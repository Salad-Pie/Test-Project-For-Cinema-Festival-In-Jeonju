export async function apiFetchWithBase(baseUrl, pathname, options = {}, fallbackMessage = 'Request failed.') {
  const mergedHeaders = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  }

  const res = await fetch(`${baseUrl}${pathname}`, {
    ...options,
    headers: mergedHeaders,
  })

  if (!res.ok) {
    throw new Error(await parseErrorResponse(res, fallbackMessage))
  }

  if (res.status === 204) return null
  const text = await res.text()
  return text ? JSON.parse(text) : null
}

export function createApiFetch(baseUrl, fallbackMessageProvider) {
  return (pathname, options = {}) => {
    const fallbackMessage = typeof fallbackMessageProvider === 'function'
      ? fallbackMessageProvider()
      : fallbackMessageProvider
    return apiFetchWithBase(baseUrl, pathname, options, fallbackMessage)
  }
}

export async function parseErrorResponse(res, fallbackMessage) {
  const text = await res.text().catch(() => '')
  if (!text) return fallbackMessage

  try {
    const payload = JSON.parse(text)
    if (typeof payload?.message === 'string' && payload.message.trim()) {
      return payload.message
    }
  } catch (_) {}

  return fallbackMessage
}
