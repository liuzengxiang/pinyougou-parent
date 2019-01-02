app.controller('searchController2',function ($scope,$location,searchService) {

    //定义搜索对象的构造
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'',pageNo:1,pageSize:10,'sort':'','sortField':''};

    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt( $scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap  = response;
                buildPageLabel()
            }
        )
    }

    buildPageLabel =function () {
        $scope.pageLabel = [];
        var stratPage = 1; //
        var endPage = $scope.resultMap.totalPages;

        $scope.startDotted = true;
        $scope.endDotted = true;

        if ($scope.resultMap.totalPages > 5){
            if ($scope.searchMap.pageNo <= 3){
                endPage = 5;
                $scope.startDotted = false;
            } else if ($scope.searchMap.pageNo >=  $scope.resultMap.totalPages - 2){
                stratPage =  $scope.resultMap.totalPages - 4;
                $scope.endDotted = false;
            }else {
                stratPage = $scope.searchMap.pageNo - 2;
                endPage = $scope.searchMap.pageNo + 2;
            }

        }else {
            $scope.startDotted = false;
            $scope.endDotted = false;
        }
        for (var i = stratPage; i <= endPage; i++) {
            $scope.pageLabel.push(i);
            
        }
    }
    //排序查询
    $scope.sortSearch = function (sortField,sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }

    $scope.queryByPage = function (page) {
        if (page <1 || page > $scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo = page;
        $scope.search();
    }

    //关键字是否是品牌
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0){
                return true;
            }
        }
        return false;
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

    $scope.loadkeywords =function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }


})