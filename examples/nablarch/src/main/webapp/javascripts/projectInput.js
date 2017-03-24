$(function() {
  $("#topUpdateButton").click(function() {
    $("#bottomUpdateButton").click();
  });

  $("#topCreateButton").click(function() {
    $("#bottomCreateButton").click();
  });

  $("#topDeleteButton").click(function() {
    $("#bottomDeleteButton").click();
  });

  $("#topBackButton").click(function() {
    $("#bottomBackButton").click();
  });

  $("#topSubmitButton").click(function() {
    $("#bottomSubmitButton").click();
  });

  $("#client_pop").click(function() {
      window.open(this.href,"clientSearch","width=700,height=500,resizable=yes,scrollbars=yes");
      return false;
  });

  $(".datepicker").datepicker();
});

function setClientParam(clientId, clientName) {
  $("[name='form.clientId']").val(clientId);
  $("[name='form.clientName']").val(clientName);
}