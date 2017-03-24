$(function() {
  // ソート条件選択
  var url = location.href;
  if (url.indexOf("?") == -1) {
    return;
  }
  var paramStr = url.split("?");
  var params = paramStr[1].split("&");
  for (var i = 0; i < params.length; i++ ) {
    var keyValue = params[i].split("=");
    if(keyValue[0] === "form.sortKey") {
      $("select[name='form.sortKey']").val(keyValue[1]);
    }
    if(keyValue[0] === "form.sortDir") {
      $("select[name='form.sortDir']").val(keyValue[1]);
    }
  }
});

$(function() {
  $('.true').remove();

 /**
  * 親ウィンドウの顧客名と顧客IDをクリアする。クリア後、ウィンドウを閉じる。
  */
  $(".clearButton").click(function() {
    window.opener.setClientParam("", "");
    window.close();
  });

  $(".closeButton").click(function() {
    window.close();
  });

 /**
  * 親ウィンドウの顧客名と顧客IDに、選択された顧客のIDと名前を設定するクリックイベントを登録する。
  * 設定後、ウィンドウを閉じる。
  */
  $(".clientLink").click(function() {
    window.opener.setClientParam($(this).children(".clientId").val(), $(this).children(".clientName").val());
    window.close();
  });
});