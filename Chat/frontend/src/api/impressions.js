async function request(path, options = {}) {
  const response = await fetch(path, {
    credentials: "include",
    ...options,
    headers: {
      ...(options.headers || {})
    }
  });

  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json")
    ? await response.json()
    : {};

  if (!response.ok) {
    throw new Error(body.message || "请求失败");
  }

  return body;
}

export function listReceivedImpressions() {
  return request("/api/impressions/received");
}

export function listGivenImpressions() {
  return request("/api/impressions/given");
}

export function addImpression(payload) {
  return request("/api/impressions", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
}
