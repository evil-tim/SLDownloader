$(document).ready(function() {
    initRunningTaskTable();
    initRunningTaskUpdater();
});

var runningTaskTable;

function initRunningTaskTable() {
    runningTaskTable = $('#runningTasks').dataTable({
        ajax : {
            url : "/tasks/running",
            dataSrc : "",
        },
        processing : false,
        serverSide : true,
        paging : false,
        info : false,
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
        } ],
        searching : false,
        lengthChange : false,
        language : {
            zeroRecords : "No Entries"
        }
    });
}

function initRunningTaskUpdater() {
    setTimeout(function() {
        runningTaskTable.api().ajax.reload();
        initRunningTaskUpdater();
    }, 5000);
}