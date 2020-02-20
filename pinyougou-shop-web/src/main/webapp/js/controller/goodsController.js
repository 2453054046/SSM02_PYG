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
	    //$location获得angularjs在网页地址列表请求的参数格式：www...#?参数名=参数值
        var id=$location.search()['id'];
       // alert(id);
	    if(id==null){
	        return;
        }
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
                //富文本编辑器添加查询的内容  editor富文本编辑器对象
                editor.html($scope.entity.goodsDesc.introduction);
                //图片
                $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
                //扩展列表
                $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //规格列表
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
                //转化SKU列表
                for(var i=0;i<$scope.entity.itemList.length;i++){
                    $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
                }
			}
		);				
	}
    /**判断规格与规格选项是否应该被勾选
     * 修改页面返回规格对应的checkbox的状态
     * @param name      规格的attributeName
     * @param value     规格的attributeValue
     * @returns {boolean}
     */
    $scope.checkAttributeValue=function(name,value){
        var items = $scope.entity.goodsDesc.specificationItems;
        //调用baseController的searchObjectBykey方法获得目前items集合中存在对应key的一条
        var object = $scope.searchObjectBykey(items,'attributeName',name);
        if(object!=null){
            //查询返回结果是否存在对应的选项
            if(object.attributeValue.indexOf(value)>=0){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }


	
	//保存 
	$scope.save=function(){
        //把富文本编辑器内的数据给扩展表的变量
        $scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
            $scope.entity.goods.isMarketable="1";
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
                    alert("保存成功");
                    location.href='goods.html';
				}else{
					alert(response.message);
				}
			}		
		);				
	}

    //增加商品
    $scope.add=function(){
	    $scope.entity.goodsDesc.introduction=editor.html();
        goodsService.add($scope.entity).success(
            function(response){
                if(response.success){
                    alert("新增成功");
                    $scope.entity={};
                    editor.html("");//清空富文本编辑器
                }else{
                    alert(response.message);
                }
            }
        );
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
	//上下架 1上架，0下架
	$scope.isMarketable=function($event){
        var a=$event.target.id;
        if(a=='up'){
            a='1';
        }else if(a=='down'){
            a='0';
        }else {
            alert("错误操作");
            return;
        }
        goodsService.isMarketable($scope.selectIds,a).success(
            function (response) {
                if(response.success){
                    $scope.reloadList();//刷新列表
                    alert(response.message);
                }else {
                    alert(response.message);
                }
            }
        )
    }

	$scope.searchEntity={};//定义搜索对象 
	
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
	$scope.uploadFile=function () {
        uploadService.uploadFile().success(
            function (response) {
                if(response.success){
                    $scope.image_entity.url=response.message;
                }else {
                    alert(response.message);
                }
            }
        )
    }
    /**
     * 定义对象格式：
     *      $scope.对象={对象：{对象:[],对象:[]},对象:{...}}
     * 定义图片列表,规格对象
     * goodsDesc:详情
     * itemImages：图片
     * specificationItems：规格
     * @type {{goodsDesc: {specificationItems: Array, itemImages: Array}}}
     */
    $scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}};

    //将当前上传的图片实体存入图片列表
    $scope.add_image_entity=function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //移除图片
    $scope.remove_image_entity=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }

    //查询一级商品分类列表
    $scope.selectItemCat1List=function(){

        itemCatService.findByParentId(0).success(
            function(response){
                $scope.itemCat1List=response;
            }
        );

    }
    /**
     * 使用监控$watch:监控$scope中所有变量的改变，当改变时执行对应方法
     * 格式：newValue：变更的值  oldValue：曾经的值
     * $scope.$watch('监控的变量',function (newValue,oldValue) {  执行的方法 })
     *
     */

    //查询二级商品分类列表
    $scope.$watch('entity.goods.category1Id',function(newValue,oldValue){

        itemCatService.findByParentId(newValue).success(
            function(response){
                $scope.itemCat2List=response;
            }
        );

    });

    //查询三级商品分类列表
    $scope.$watch('entity.goods.category2Id',function(newValue,oldValue){

        itemCatService.findByParentId(newValue).success(
            function(response){
                $scope.itemCat3List=response;
            }
        );

    });


    //读取模板ID
    $scope.$watch('entity.goods.category3Id',function(newValue,oldValue){

        itemCatService.findOne(newValue).success(
            function(response){
                $scope.entity.goods.typeTemplateId=response.typeId;
            }
        );
    });
    
    //读取模板ID后，读取品牌列表,扩展属性，规格列表
    $scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate=response;//模板对象
                $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);//品牌列表转换
                //扩展属性,判断当前是否是更改页面$location.search读取angularjs请求的参数
                if($location.search()['id']==null){
                    $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
                }
        });
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList=response;

            }
        );
    })

    /**
     * 规格选中和取消选中
     * 调用baseController中searchObjectBykey集合搜索方法判断是否存在name和value
     * @param name      specificationItems中attrbuteName的值
     * @param value     specificationItems中attributeValue的值
     * specificationItems的结构：
     *              [{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]
     */
    $scope.updateSpecAttribute=function ($event,name,value) {
        var object = $scope.searchObjectBykey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
        if(object!=null){
            //判断复选框是否选中
            if($event.target.checked){
                object.attributeValue.push(value);
            }else {
                object.attributeValue.splice(object.attributeValue.indexOf(value),1);
                //如果选项都取消了，将此条记录移除
                if(object.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object,1))
                }
            }
        }else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
        }
    }

    //创建SKU列表
    $scope.createItemList=function () {
        //spec：选中的规格信息  price：价格  num：库存  status：状态   isDefault：是否选中
        $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'}]//列表初始化
        //获得用户选中的规格选项信息
        var items=$scope.entity.goodsDesc.specificationItems;
        //遍历规格信息，加入SKU列表集合中
        for(var i=0;i<items.length;i++){
            $scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
        }
    }
    /**
     * 将规格列表信息放入SKU列表集合中
     * @param list          SKU列表集合
     * @param columnName    规格的attrbuteName
     * @param columnValues  规格的attrbuteValue
     * 遍历列表，然后遍历attrbuteValue的数据，把数据放入集合的spec属性中key是attrbuteName
     */
    addColumn=function (list,columnName,columnValues) {
        var newList=[];
        for(var i=0;i<list.length;i++){
            var oldRow=list[i];
            for(var j=0;j<columnValues.length;j++){
                /**
                 * 深度克隆：将一个对象转化为JSON数据，然后再次转换成对象格式赋值给一个新的对象
                 * 优点：当更改新的对象信息时，不对带动被克隆对象的信息
                 */
                var newRow = JSON.parse(JSON.stringify(oldRow));
                newRow.spec[columnName]=columnValues[j];//将所有columnValues的数据放入新规格列表的spec中，name是columnName
                newList.push(newRow);
            }
        }
        return newList;
    }
    $scope.status=['未审核','已审核','审核未通过','已关闭'];
    $scope.itemCatList=[];//存储商品分类的name
    //查询商品分类列表
    $scope.findItemCatList=function () {
        itemCatService.findAll().success(
            function (response) {
                for(var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name;
                }
            }
        )
    }
});	
