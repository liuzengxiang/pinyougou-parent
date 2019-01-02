app.controller('baseController',function (    $window,$scope) {

    $scope.reload = function () {
        $window.location.reload();
    }
    //分页插件
    $scope.paginationConf = {
        currentPage:1,
        totalItems:10,
        itemsPerPage:10,
        perPageOptions:[10,20,30,40],
        onChange:function () {
            $scope.reloadList();
        }
    };
    //reload
    $scope.reloadList = function () {
        // $scope.findPage($scope.paginationConf.currentPage , $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage , $scope.paginationConf.itemsPerPage);
    };
    //存复选框
    $scope.selectIds = [];
    //获取选中的复选框
    $scope.updateSelection = function ($event,id) {
        if ($event.target.checked){
            $scope.selectIds.push(id);
        }else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);
        }
    };



    $scope.jsonToString = function(jsonString,key){
        var json= JSON.parse(jsonString);
        var value="";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+="| ";
            }
            value += json[i][key];
        }
        return value;
    }
});