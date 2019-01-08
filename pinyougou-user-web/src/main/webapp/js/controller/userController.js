 //控制层 
app.controller('userController' ,function($scope,$controller,userService){


	$scope.reg = function () {
		if ($scope.password != $scope.entity.password){
			alert("密码不一致，重新输入");
			$scope.entity.password ="";
			$scope.password = "";
			return ;
		}

        userService.add($scope.entity,$scope.smscode).success(
        	function (response) {
				if (response.success){
					alert(response.message)
				}else {
					alert(response.message)
				}
            }
		)
    }

    sendCode = function () {
		userService.sendCode($scope.entity.phone).success(
			function (response) {
                if (response.success){
                    alert(response.message)
                }else {
                    alert(response.message)
                }
            }
		)

    };

     $scope.checkPhone = function () {
         var mobile = $scope.entity.phone;

        if(mobile == undefined || mobile.length == 0 || mobile ==null ) {
            alert('请输入手机号码！');
            return false;
        }
        if(mobile.length!=11) {
            alert('请输入有效长度的手机号码！');
            return false;
        }
        var myreg = '^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,2,3,5-9]))\\d{8}$';
         var re = new RegExp(myreg);

     /*   alert(mobile);
        alert(re.test(mobile));
*/
         if(!re.test(mobile)) {
            alert('请输入有效的手机号码！');
            return false;
         }
        sendCode();
    }


	/*$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		userService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		userService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		userService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=userService.update( $scope.entity ); //修改  
		}else{
			serviceObject=userService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		userService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		userService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}*/
    
});	
