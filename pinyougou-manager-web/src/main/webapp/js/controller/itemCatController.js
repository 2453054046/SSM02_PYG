 //控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;
                //设置下拉框的默认值
                var arr=[];
                arr[0]=response.typeId;
                $('#select1').val(arr[0]).trigger('change');

			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
            $scope.entity.typeId=$scope.entity.typeId.id;
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
            $scope.entitys();
            $scope.entity.parentId=$scope.entityOne.id;
			serviceObject=itemCatService.add( $scope.entity);//增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
				    if($scope.grade>1){
                        $scope.grade-=1;
                    }
                    $scope.grade-=1;
				    if($scope.entityOne.id!=0){
                        $scope.selectList($scope.entityOne);
                    }else {
                        //重新查询 
                        $scope.reloadList();//重新加载
                    }

				}else{
					alert(response.message);
				}
			}		
		);				
	}
    //判断面包屑栏是否有值
	$scope.entitys=function(){
	    $scope.entityOne;
        if($scope.entity_1!=null){
            $scope.entityOne = $scope.entity_1;
        }
        if($scope.entity_2!=null){
            $scope.entityOne = $scope.entity_2;
        }
        if($scope.entity_1==null&$scope.entity_2==null){
            $scope.entityOne.id = 0;
        }
    }
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
                    $scope.entitys();
                    if($scope.entityOne.id!=0){
                        $scope.selectList($scope.entityOne);
                    }else {
                        scope.reloadList();//刷新列表
                    }
					$
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    //根据上级分类查询商品分类列表
    $scope.findByParentId=function (parentId) {
        itemCatService.findByParentId(parentId).success(
          function (response) {
              $scope.list=response;
          }  
        );
    }

    $scope.grade=1;//当前级别
    //设置级别
    $scope.setGrade=function(value){
        $scope.grade=value;
    }

    //面包屑导航
    $scope.selectList=function(p_entity){
        //alert($scope.grade);

        if($scope.grade==1){
            $scope.entity_1=null;
            $scope.entity_2=null;
        }
        if($scope.grade==2){

            $scope.entity_1=p_entity;
            $scope.entity_2=null;
        }
        if($scope.grade==3){
            $scope.entity_2=p_entity;
        }
        $scope.findByParentId(p_entity.id);
    }

    //初始化商品类型模板下拉列表
    $scope.typeTemplateLists={data:[]};
    $scope.typeTemplateLists=function () {
        typeTemplateService.selectOptionList().success(
            function (response) {
                $scope.typeTemplateLists = {data:response};

            }
        )
    }
});	
