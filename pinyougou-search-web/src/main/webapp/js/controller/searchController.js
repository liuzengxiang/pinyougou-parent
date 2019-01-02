app.controller('searchController',function ($scope,searchService) {

    //定义搜索对象的构造
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':''};

    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap  = response;
            }
        )
    }

    //改变搜索值
    $scope.addSearchItem = function (key,value) {
        if (key == 'category' || key == 'brand' || key == 'price'){//点击分类或者品牌
            $scope.searchMap[key]  = value;
        }else {//点击的规格
            // $scope.searchMap.spec ={ key:value};
            $scope.searchMap.spec[key] = value;
        }
        $scope.search()
    }

    //撤销搜索项
    $scope.removeSearchItem =function (key) {
        if (key == 'category' || key == 'brand' || key == 'price'){//点击分类或者品牌
            $scope.searchMap[key]  = "";
        }else {//点击的规格
            delete  $scope.searchMap.spec[key] ;
        }
        $scope.search()
    }


})