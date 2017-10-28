$(document).ready(function() {
    $('#allTasks').dataTable({
        ajax : {
            url : "/tasks",
            dataFilter : function(data) {
                return convertToSpringDataResult(data);
            },
            data : function(params) {
                convertToSpringDataParams(params);
            }
        },
        processing : false,
        serverSide : true,
        columns : [ {
            name : "id",
            data : "id"
        }, {
            name : "dateFrom",
            data : "dateFrom"
        }, {
            name : "dateTo",
            data : "dateTo"
        }, {
            name : "fund",
            data : "fund"
        }, {
            name : "status",
            data : "status"
        } ],
        searching : false,
        lengthChange : false,
        pageLength : 20,
        searchCols : [ null, null, null, null, null ],
        createdRow : function(row, data, dataIndex) {
            if (data.status === "FAILED") {
                $(row).addClass('danger');
            } else if (data.status === "PENDING") {
                $(row).addClass('warning');
            } else if (data.status === "SUCCESS") {
                $(row).addClass('success');
            }
        }
    });
});

function convertToSpringDataParams(params) {

    // page size
    params.size = params.length;

    // sort
    params.order && params.order.map(function(sortItem) {
        var sortColName = params.columns[sortItem.column].name;
        params.sort = sortColName + "," + sortItem.dir;
    });

    // page
    params.page = params.start / params.size;

}

function convertToSpringDataResult(data) {
    var json = jQuery.parseJSON(data);

    // convert totals
    json.recordsTotal = json.totalElements;
    json.recordsFiltered = json.totalElements;

    // convert data
    json.data = json.content;

    return JSON.stringify(json);
}