const form = document.querySelector("#loginForm");
const message = document.querySelector("#message");

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  message.textContent = "";
  message.classList.remove("success");

  const formData = new FormData(form);
  const response = await fetch("/api/auth/login", {
    method: "POST",
    body: new URLSearchParams(formData)
  });

  const result = await response.json();
  if (!response.ok) {
    message.textContent = result.message || "登录失败";
    return;
  }

  message.classList.add("success");
  message.textContent = "登录成功，正在进入系统";
  window.setTimeout(() => {
    window.location.href = "/home.html";
  }, 500);
});
