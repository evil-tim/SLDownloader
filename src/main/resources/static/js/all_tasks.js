$(document).ready(function() {
    initAllTaskFundFilter();
    initAllTaskDateFilter();
    initAllTaskTable();
    initAllTaskFilterUpdateEvents();
});

var allTaskTable;

function initAllTaskTable() {
    allTaskTable = $('#allTasks').dataTable({
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
            data : "fundName"
        }, {
            name : "status",
            data : "status"
        } ],
        searching : false,
        lengthChange : false,
        pageLength : 20,
        searchCols : [ null, null, null, null, null ],
        createdRow : function(row, data, dataIndex) {
            if (data.status === "FAILED" && data.retryable === true) {
                $(row).addClass('warning');
            } else if (data.status === "FAILED" && data.retryable === false) {
                $(row).addClass('danger');
            } else if (data.status === "PENDING") {
                $(row).addClass('info');
            } else if (data.status === "SUCCESS") {
                $(row).addClass('success');
            }
        },
        language : {
            zeroRecords : "No Entries"
        }
    });
}

function convertToSpringDataParams(params) {

    // page size
    params.size = params.length;
    delete params.length;

    // sort
    params.order && params.order.map(function(sortItem) {
        var sortColName = params.columns[sortItem.column].name;
        params.sort = sortColName + "," + sortItem.dir;
    });
    delete params.order;
    delete params.columns;

    // page
    params.page = params.start / params.size;

    // filters
    var dateFilter = $('#dateFilter').datepicker('getDate');

    params.date = dateFilter ? dateFilter.toISOString().slice(0, 10) : null;
    params.fund = $('#fundFilter').val();
    params.status = $('#statusFilter').val();
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

function initAllTaskFundFilter() {
    $.ajax({
        url : "/fund/all"
    }).done(updateAllTaskFundFilter);
}

function updateAllTaskFundFilter(data) {
    for (var i = 0; i < data.length; i++) {
        $("#fundFilter").append(
                $("<option></option>").attr("value", data[i].code).text(
                        data[i].name));
    }
    $('#fundFilter').selectpicker('refresh');
}

function initAllTaskDateFilter() {
    $('#dateFilter').datepicker({
        container : "html",
        autoclose : true,
        clearBtn : true,
        format : "yyyy-mm-dd",
    });
}

function initAllTaskFilterUpdateEvents() {
    $('#dateFilter').datepicker().on('changeDate', function() {
        allTaskTable.api().ajax.reload();
    });
    $('#fundFilter').on('change', function() {
        allTaskTable.api().ajax.reload();
    });
    $('#statusFilter').on('change', function() {
        allTaskTable.api().ajax.reload();
    });
    $('#allTasksRefresh').on('click', function() {
        allTaskTable.api().ajax.reload();
    });
}