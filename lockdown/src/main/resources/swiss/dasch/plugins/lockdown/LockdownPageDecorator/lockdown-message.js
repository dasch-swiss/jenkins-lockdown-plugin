const lockdownMessageDiv = document.getElementById("swiss_dasch_plugins_lockdown_message");
const viewMessageDiv = document.getElementById("view-message");
const systemMessageDiv = document.getElementById("systemmessage");
if(lockdownMessageDiv != null && viewMessageDiv != null && systemMessageDiv != null && viewMessageDiv.contains(systemMessageDiv)) {
  viewMessageDiv.insertBefore(lockdownMessageDiv, systemMessageDiv.nextSibling);
  lockdownMessageDiv.style.display = "initial";
}