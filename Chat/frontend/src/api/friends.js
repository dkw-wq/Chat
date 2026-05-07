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

export function searchUsers(keyword) {
  return request(`/api/users/search?keyword=${encodeURIComponent(keyword)}`);
}

export function listFriends() {
  return request("/api/friends");
}

export function deleteFriend(friendId) {
  return request(`/api/friends/${friendId}`, {
    method: "DELETE"
  });
}

export function sendFriendRequest(payload) {
  return request("/api/friends/requests", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
}

export function listIncomingRequests() {
  return request("/api/friends/requests/incoming");
}

export function listOutgoingRequests() {
  return request("/api/friends/requests/outgoing");
}

export function acceptFriendRequest(requestId) {
  return request(`/api/friends/requests/${requestId}/accept`, {
    method: "POST"
  });
}

export function rejectFriendRequest(requestId) {
  return request(`/api/friends/requests/${requestId}/reject`, {
    method: "POST"
  });
}
