app.service('uploadService',function ($http) {

    //上传文件
    this.uploadFile=function () {
        //html5新增类用于上传文件
        var formdata=new FormData();
        formdata.append('file',file.files[0]);//file 文件上传框获得第一个上传框
        return $http({
            url:'../upload.do',
            method:'post',
            data:formdata,//数据是二进制文件
            headers:{'Content-Type':undefined},//指定上传的不是json数据，默认上传为json数据
            transformRequest:angular.identity//对整个表单进行二进制序列化
        });
    }
})