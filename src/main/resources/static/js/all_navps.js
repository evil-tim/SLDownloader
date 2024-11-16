$(document).ready(function() {
    initAllNavpsFundFilter();
    initAllNavpsDateFilter();
    initAllNavpsTable();
    initAllNavpsFilterUpdateEvents();
});

var allNavpsTable;

function initAllNavpsTable() {
    allNavpsTable = $('#allNavps').dataTable({
        ajax : {
            url : "/api/navps",
            dataFilter : function(data) {
                return convertNavpsToSpringDataResult(data);
            },
            data : function(params) {
                convertToNavpsSpringDataParams(params);
            }
        },
        processing : false,
        serverSide : true,
        columns : [ {
            name : "date",
            data : "entryDate"
        }, {
            name : "fund",
            data : "fundName",
            orderable : false
        }, {
            name : "value",
            data : "fundValue",
            orderable : false
        } ],
        searching : false,
        lengthChange : false,
        pageLength : 20,
        searchCols : [ null, null, null ],
        order : [ [ 0, "desc" ] ],
        language : {
            zeroRecords : "No Entries"
        }
    });
}

function convertToNavpsSpringDataParams(params) {

    delete params.search;

    // page size
    params.size = params.length;
    delete params.length;

    // sort
    params.order && params.order.map(function(sortItem) {
        var sortColName = params.columns[sortItem.column].data;
        params.sort = sortColName + "," + sortItem.dir;
    });
    delete params.order;
    delete params.columns;

    // page
    params.page = params.start / params.size;

    // filters
    var dateFromFilter = $('#dateFromFilter').datepicker('getDate');
    if (dateFromFilter) {
        var dateFromFilterAdjusted = new Date(dateFromFilter);
        dateFromFilterAdjusted.setMinutes(dateFromFilter.getMinutes()
                - dateFromFilter.getTimezoneOffset());
        dateFromFilter = dateFromFilterAdjusted;
    }

    var dateToFilter = $('#dateToFilter').datepicker('getDate');
    if (dateToFilter) {
        var dateToFilterAdjusted = new Date(dateToFilter);
        dateToFilterAdjusted.setMinutes(dateToFilter.getMinutes()
                - dateToFilter.getTimezoneOffset());
        dateToFilter = dateToFilterAdjusted;
    }

    params.dateFrom = dateFromFilter ? dateFromFilter.toISOString()
            .slice(0, 10) : null;
    params.dateTo = dateToFilter ? dateToFilter.toISOString().slice(0, 10)
            : null;
    params.fund = $('#fundFilter').val();
}

function convertNavpsToSpringDataResult(data) {
    var json = jQuery.parseJSON(data);

    // convert totals
    json.recordsTotal = json.totalElements;
    json.recordsFiltered = json.totalElements;

    // convert data
    json.data = json.content;

    return JSON.stringify(json);
}

function initAllNavpsFundFilter() {
    $.ajax({
        url : "/api/funds"
    }).done(updateAllNavpsFundFilter);
}

function updateAllNavpsFundFilter(data) {
    for (let i = 0; i < data.length; i++) {
        $("#fundFilter").append(
                $("<option></option>").attr("value", data[i].code).text(
                        data[i].name));
    }
    $('#fundFilter').selectpicker('refresh');
}

function initAllNavpsDateFilter() {
    $('#dateFromFilter').datepicker({
        container : "body",
        autoclose : true,
        clearBtn : true,
        format : "yyyy-mm-dd",
    });
    $('#dateToFilter').datepicker({
        container : "body",
        autoclose : true,
        clearBtn : true,
        format : "yyyy-mm-dd",
    });
}

function initAllNavpsFilterUpdateEvents() {
    $('#dateFromFilter').datepicker().on('changeDate', function() {
        allNavpsTable.api().ajax.reload();
    });
    $('#dateToFilter').datepicker().on('changeDate', function() {
        allNavpsTable.api().ajax.reload();
    });
    $('#fundFilter').on('change', function() {
        allNavpsTable.api().ajax.reload();
    });
    $('#statusFilter').on('change', function() {
        allNavpsTable.api().ajax.reload();
    });
    $('#allNavpsRefresh').on('click', function() {
        allNavpsTable.api().ajax.reload();
    });
}