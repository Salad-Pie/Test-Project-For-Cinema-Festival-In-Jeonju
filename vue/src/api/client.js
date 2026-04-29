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
    throw new Error(fallbackMessage)
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
  await res.text().catch(() => '')
  return fallbackMessage
}
