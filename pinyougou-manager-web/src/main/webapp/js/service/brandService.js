app.service("brandService",function ($http) {
    this.findAll=function () {
        return $http.get('../brand/findAll.do');
    }
    this.pageAll=function (page,size) {
        return $http.get('../brand/pageAll.do?page='+page+'&size='+size);
    }
    this.search=function (page,size,searchEntity) {
        return $http.post('../brand/search.do?page='+page+'&size='+size,searchEntity);
    }
    this.add=function (entity) {
        return $http.post('../brand/add.do',entity);
    }
    this.update=function (entity) {
        return $http.post('../brand/update.do',entity);
    }
    this.findById=function (id) {
        return $http.get('../brand/findById.do?id='+id)
    }
    this.del=function (id) {
        return $http.get('../brand/del.do?ids='+id);
    }
    this.selectOptionList=function () {
        return $http.get('../brand/selectOptionList.do');
    }
});