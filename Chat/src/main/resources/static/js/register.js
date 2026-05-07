const form = document.querySelector("#registerForm");
const message = document.querySelector("#message");

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  message.textContent = "";
  message.classList.remove("success");

  const data = Object.fromEntries(new FormData(form).entries());
  const response = await fetch("/api/auth/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });

  const result = await response.json();
  if (!response.ok) {
    message.textContent = result.message || "注册失败";
    return;
  }

  message.classList.add("success");
  message.textContent = "注册成功，正在前往登录";
  window.setTimeout(() => {
    window.location.href = "/login.html";
  }, 700);
});
