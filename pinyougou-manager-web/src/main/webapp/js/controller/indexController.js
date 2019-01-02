app.controller("indexController",function ($scope,loginService,uploadService) {
    $scope.showLoginName = function () {
        loginService.loginName().success(function (response) {
            $scope.loginName = response.loginName;
        })
    }

    $scope.changeImg = function () {
        // alert(1)
    }
    $scope.touxiang = function () {
        $scope.image= '../img/lzx/1.jpg';
    }

    //上传图片
    $scope.uploadFile=function(){
        uploadService.uploadFile().success(
            function(response){
                if(response.success){
                    // alert(response.message);
                    $scope.image = response.message;
                }else{
                    alert(response.message);
                }
            }
        );
    }

})