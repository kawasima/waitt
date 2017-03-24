$(function() {
  //ページングタグにより自動生成される要素を削除している
  $('.true').remove();
});
$(function() {
  // ソート条件選択
  var url = location.href;
  if (url.indexOf("?") == -1) {
    $("select[name='searchForm.sortKey']").val("name");
    $("select[name='searchForm.sortDir']").val("asc");
    return;
  }
  var paramStr = url.split("?");
  var params = paramStr[1].split("&");
  for (var i = 0; i < params.length; i++ ) {
    var keyValue = params[i].split("=");
    if(keyValue[0] === "searchForm.sortKey") {
      if (keyValue[1] === "") {
        $("select[name='searchForm.sortKey']").val("name");
      } else {
        $("select[name='searchForm.sortKey']").val(keyValue[1]);
      }
    }
    if(keyValue[0] === "searchForm.sortDir") {
      if (keyValue[1] === "") {
        $("select[name='searchForm.sortDir']").val("asc");
      } else {
        $("select[name='searchForm.sortDir']").val(keyValue[1]);
      }
    }
  }
});
$(function() {
  // ソート条件
  $("#sortKey").change(function() {
      $(this).parents('form').submit();
  });
  $("#sortDir").change(function() {
      $(this).parents('form').submit();
  });
});