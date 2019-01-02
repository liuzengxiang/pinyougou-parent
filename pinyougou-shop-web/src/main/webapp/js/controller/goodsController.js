 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id = $location.search()['id']
        if(id==null){
            return ;
        }
        goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;

                editor.html($scope.entity.goodsDesc.introduction );
                //图片
                $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
                //扩展
                $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //规格
                $scope.entity.goodsDesc.specificationItems= JSON.parse($scope.entity.goodsDesc.specificationItems);
                //循环把SKU字符串转换成json
                for(var i=0;i< $scope.entity.itemList.length;i++ ){
                    $scope.entity.itemList[i].spec=  JSON.parse($scope.entity.itemList[i].spec);
                }


			}
		);				
	}
	//保存 
	$scope.save=function(){
        $scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象
        // alert($scope.entity.goods.id)
		if($scope.entity.goods.id != null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
                    alert("保存成功");
                    location.href ="goods.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	}


    //新增
    $scope.add =function () {
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {
                    alert("保存成功");
                    $scope.entity={};
                    editor.html("");
                } else {
                    alert(response.message);
                }
            })
    }

	//批量删除
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}

    //定义搜索对象
	$scope.searchEntity={};

	//搜索
	$scope.search=function(page,rows){
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}

    //上传图片
    $scope.uploadFile=function(){
        uploadService.uploadFile().success(
            function(response){
                if(response.success){
                	// alert(response.message);
                    $scope.image_entity.url = response.message;
                }else{
                    alert(response.message);
                }
            }
        );
    }

    //定义组合实体类
	$scope.entity = {goods:{},goodsDesc:{itemImages:[], specificationItems:[]}};

    //将当前上传的图片存起来
    $scope.add_image_entity = function () {
		$scope.entity.goodsDesc.itemImages.push( $scope.image_entity);
    }

	//移除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }

    //查询一级列表分类
	$scope.selectItemCat1List =function () {
		itemCatService.findByParentId(0).success(
			function (response) {
				$scope.itemCat1List = response;

            }
		)
    }

    // 二级分类
    $scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
        if (newValue != undefined) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                   // $scope.itemCat3List = [];
                    //$scope.entity.goods.typeTemplateId = '';
                    $scope.itemCat2List = response;
                }
            )
        }

    });

    //三级分类
    $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
        // alert("三级:"+newValue);
        if (newValue != undefined) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List = response;
                }
            )
        }
    });

/*    //模版ID   遍历拿
    $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
        alert(newValue);
        if ($scope.itemCat3List != null) {
            for (var i = 0; i < $scope.itemCat3List.length; i++) {
                if ($scope.itemCat3List[i].id == newValue) {
                    $scope.entity.goods.typeTemplateId = $scope.itemCat3List[i].typeId;
                }
            }
        }
    });*/

    //模版ID findOne

    $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
        // alert("摸办:"+newValue);
        itemCatService.findOne(newValue).success(
            function (response) {
                // alert(response.typeId)
                $scope.entity.goods.typeTemplateId = response.typeId;
            }
        )
    });


    $scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
      typeTemplateService.findOne(newValue).success(
      	function (response) {
			$scope.typeTemplate = response;
			$scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);

            if( $location.search()['id']==null ){//如果是增加商品
                $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
            }
        }
	  );

		typeTemplateService.findSpecList(newValue).success(
			function (response) {
				$scope.specList = response;
            }
		)
    })

    $scope.updateSpecAttribute = function ($event,name, value) {
       var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		if (object != null){
       		if ($event.target.checked){
                object.attributeValue.push(value);
            }else{
       			var optionIndex = object.attributeValue.indexOf(value);
                object.attributeValue.splice(optionIndex,1);
                if ( object.attributeValue.length == 0){
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(object),1
					)
				}
			}
		}else {
            $scope.entity.goodsDesc.specificationItems.push({'attributeName':name,attributeValue:[value]})
		}
    }

	//创建SKU列表
    $scope.createItemList = function () {
    	//初始化列表 字段spec是需要改变的内容
		$scope.entity.itemList = [{spec:{},price:0,num:9999,status:'0',isDefault:'0'}];
		//浅克隆
		var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue)
        }
    };

    //添加行
    addColumn = function (list,columnName,columnValues) {
		var newList = [];
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < columnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[columnName] = columnValues[j];
				newList.push(newRow);
            }
        }
		return newList;
    };

    //状态信息
    $scope.status = ['未审核','已审核','已驳回','已关闭']

	//商品分类列表
	$scope.itemCatList= [];
    //查询分类名称
	$scope. findItemCatList = function () {
		itemCatService.findAll().success(
			function (response) {
                for (var i = 0; i < response.length; i++) {
                    var obj = response[i];
                    $scope.itemCatList[obj.id] = obj.name;
                }
            }
		)
    }

    //判断选中状态
    $scope.checkAttributeValue = function (specName,optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items,'attributeName',specName)

        if (object != null){
            if (object.attributeValue.indexOf(optionName) >=0){
                return true;
            }else {}
            return false;
        }else {
            return false;
        }

    }
});	
