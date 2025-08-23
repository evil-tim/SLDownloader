$(document).ready(function() {
    initAllTaskFundFilter();
    initAllTaskDateFilter();
    initAllTaskTable();
    initAllTaskFilterUpdateEvents();
    initEvents();
});

function initEvents() {
    $("#allTasks").on("click", ".retry-btn", requestRetryTask);
}

var allTaskTable;

function initAllTaskTable() {
    allTaskTable = $('#allTasks').dataTable({
        ajax : {
            url : "/api/tasks",
            dataFilter : function(data) {
                return convertToTasksSpringDataResult(data);
            },
            data : function(params) {
                convertToTasksSpringDataParams(params);
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
        }, {
            name : "actions",
            orderable: false,
            render: function (_data, _type, row, _meta) {
                return "<button class=\"btn btn-default btn-sm retry-btn\" " +
                    "style=\"display:none; padding-top: 1px; padding-bottom: 1px\" " +
                    "data-taskid=\"" + row.id + "\">Retry</button>"
            }
        } ],
        searching : false,
        lengthChange : false,
        pageLength : 20,
        searchCols : [ null, null, null, null, null, null ],
        order : [ [ 2, "desc" ] ],
        createdRow : function(row, data, _dataIndex) {
            if (data.status === "FAILED" && data.retryable === true) {
                $(row).addClass('warning');
            } else if (data.status === "FAILED" && data.retryable === false) {
                $(row).addClass('danger force-retry');
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

function convertToTasksSpringDataParams(params) {

    delete params.search;

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
    if (dateFilter) {
        var dateFilterAdjusted = new Date(dateFilter);
        dateFilterAdjusted.setMinutes(dateFilter.getMinutes()
                - dateFilter.getTimezoneOffset());
        dateFilter = dateFilterAdjusted;
    }

    params.date = dateFilter ? dateFilter.toISOString().slice(0, 10) : null;
    params.fund = $('#fundFilter').val();
    params.status = $('#statusFilter').val();
}

function convertToTasksSpringDataResult(data) {
    var json = jQuery.parseJSON(data);

    // convert totals
    json.recordsTotal = json.page.totalElements;
    json.recordsFiltered = json.page.totalElements;

    // convert data
    json.data = json.content;

    return JSON.stringify(json);
}

function initAllTaskFundFilter() {
    $.ajax({
        url : "/api/funds"
    }).done(updateAllTaskFundFilter);
}

function updateAllTaskFundFilter(data) {
    for (let i = 0; i < data.length; i++) {
        $("#fundFilter").append(
                $("<option></option>").attr("value", data[i].code).text(
                        data[i].name));
    }
    $('#fundFilter').selectpicker('refresh');
}

function initAllTaskDateFilter() {
    $('#dateFilter').datepicker({
        container : "body",
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

function requestRetryTask(event) {
    var id = $(event.target).data("taskid");
    if(id) {
        $.ajax({
            type: "POST",
            url : "/api/task/" + id + "/retry"
        }).done(function() {
            allTaskTable.api().ajax.reload();
        });
    }
}