

window.onload = function() {

    var rejectButtons = document.getElementsByClassName('Reject');

    for(var i=0; i<rejectButtons.length; i++){
        (
            function(object){
                object.onclick = function(){

                    var row = object.parentElement.parentElement;
                    var data = row.getElementsByTagName('td');

                    var title = document.getElementById('rejectDataModalTitle');
                    title.innerHTML = "Reject transaction, id: " + data[0].innerHTML;

                    var modalBody = document.getElementById('rejectDataModalBody');
                    var divs = modalBody.getElementsByTagName('div');
                    var target = []
                    for(var j=0; j<divs.length; j++) target.push(divs[j].getElementsByTagName('input')[0]);

                    for(var j =0; j<data.length-2; j++){
                        var k = j;
                        if(j > 4) k = k + 1;
                        target[k].value = data[j].innerHTML;
                    }
                    target[5].value = "";
                }
            }
        )(rejectButtons[i]);
    }



    var approveWButtons = document.getElementsByClassName('ApproveWithdraw');

    for(var i=0; i<approveWButtons.length; i++){

        (
            function(object){

                object.onclick = function(){

                    var row = object.parentElement.parentElement;
                    var data = row.getElementsByTagName('td');
                    var id = data[0].innerHTML;

                    var title = document.getElementById('approveDataModalTitle');
                    title.innerHTML = "Approve transaction, id: " + id;

                    var modalBody = document.getElementById('approveDataModalBody');
                    var divs = modalBody.getElementsByTagName('div');
                    var targetCols = [];
                    for(var j=0; j<divs.length; j++) targetCols.push(divs[j].getElementsByTagName('input')[0]);

                    for(var j=0; j<data.length-2; j++) targetCols[j].value = data[j].innerHTML;

                    targetCols[7].disabled = false;
                    targetCols[8].disabled = false;

                    targetCols[7].value = "";
                    targetCols[8].value = "";
                }
            }
        )(approveWButtons[i]);
    }

    var approveBButtons = document.getElementsByClassName('ApproveBuy');

    for(var i=0; i<approveBButtons.length; i++){

        (
            function(object){

                object.onclick = function(){

                    var p = object.parentElement;
                    var buttons = p.getElementsByTagName('button');
                    for(var j=0; j<buttons.length-1; j++) buttons[j].disabled = true;

                    var row = object.parentElement.parentElement;
                    var columns = row.getElementsByTagName('td');
                    var targetCol = columns[columns.length-2];

                    var lists = targetCol.getElementsByTagName('ul')[0].getElementsByTagName('li');
                    var inputs = targetCol.getElementsByTagName('input');

                    var id = columns[0].innerHTML;
                    var type = columns[3].innerHTML;

                    lists[0].innerHTML = "Action: " + "Will be approved";
                    lists[0].style.display = "block";

                    inputs[0].value = id;
                    inputs[0].disabled = false;

                    inputs[1].value = type;
                    inputs[1].disabled = false;

                    inputs[6].disabled = false;
                    inputs[6].value = "approve";
                }
            }
        )(approveBButtons[i]);
    }



    var refundButtons = document.getElementsByClassName('Refund');

    for(var i=0; i<refundButtons.length; i++){

        (
            function(object){

                object.onclick = function(){

                    var row = object.parentElement.parentElement;
                    var data = row.getElementsByTagName('td');
                    var id = data[0].innerHTML;

                    var title = document.getElementById('refundDataModalTitle');
                    title.innerHTML = "Refund transaction, id: " + id;

                    var modalBody = document.getElementById('refundDataModalBody');
                    var divs = modalBody.getElementsByTagName('div');
                    var targetCols = [];
                    for(var j=0; j<divs.length; j++) targetCols.push(divs[j].getElementsByTagName('input')[0]);

                    for(var j=0; j<data.length-2; j++) {
                        var k = j;
                        if(j > 2) k += 2;
                        targetCols[k].value = data[j].innerHTML;
                    }

                    targetCols[3].value = "";
                    targetCols[4].value = "";
                }
            }
        )(refundButtons[i]);
    }




    var cancelButtons = document.getElementsByClassName('Cancel');

    for(var i=0; i<cancelButtons.length; i++){

        (
            function(object){

                object.onclick = function(){

                    var row = object.parentElement.parentElement;
                    var columns = row.getElementsByTagName('td');
                    var column = columns[columns.length-2];

                    var buttonCol = columns[columns.length-1];
                    var buttons = buttonCol.getElementsByTagName('button');

                    for(var j=0; j<buttons.length; j++) buttons[j].disabled = false;

                    var lists = column.getElementsByTagName('ul')[0].getElementsByTagName('li');
                    for(var j=0; j<lists.length; j++) {
                        lists[j].style.display = "none";
                        lists[j].innerHTML = "";
                    }

                    var input = column.getElementsByTagName('input');
                    for(var j=0; j<input.length; j++) {
                        input[j].value = "";
                        input[j].disabled = true;
                    }
                }
            }
        )(cancelButtons[i]);
    }
}


function approveSaveClick(){

    var modalBody = document.getElementById('approveDataModalBody');
    var divs = modalBody.getElementsByTagName('div');

    var inputs = [];
    for(var i=0; i<divs.length; i++) inputs.push(divs[i].getElementsByTagName('input')[0]);

    var id = inputs[0].value;
    var transactionId = inputs[7].value;
    var sender = inputs[8].value;

    if(transactionId != "" && sender != ""){

        var row = document.getElementById('row' + id);
        var data = row.getElementsByTagName('td');

        var buttons = data[data.length-1].getElementsByTagName('button');
        for(var i=0; i<buttons.length-1; i++) buttons[i].disabled = true;

        var lists = data[data.length-2].getElementsByTagName('ul')[0].getElementsByTagName('li');
        var inputs = data[data.length-2].getElementsByTagName('input');

        inputs[0].disabled = false;
        inputs[0].value = id;

        inputs[1].disabled = false;
        inputs[1].value = "withdraw";

        inputs[2].disabled = false;
        inputs[2].value = transactionId;

        inputs[3].disabled = false;
        inputs[3].value = sender;

        inputs[6].disabled = false;
        inputs[6].value = "approve";

        lists[0].style.display = "block";
        lists[1].style.display = "block";
        lists[2].style.display = "block";

        lists[0].innerHTML = "Transaction id: " + transactionId;
        lists[1].innerHTML = "Sender: " + sender;
        lists[2].innerHTML = "Action: " + "Will be approved";
    }
}

function refundSaveClick(){

    var modalBody = document.getElementById('refundDataModalBody');
    var divs = modalBody.getElementsByTagName('div');

    var inputs = [];
    for(var i=0; i<divs.length; i++) inputs.push(divs[i].getElementsByTagName('input')[0]);

    var id = inputs[0].value;
    var refundAmount = inputs[3].value;
    var reason = inputs[4].value;

    if(refundAmount != "" && reason != ""){

        var row = document.getElementById('row' + id);
        var data = row.getElementsByTagName('td');

        var buttons = data[data.length-1].getElementsByTagName('button');
        for(var i=0; i<buttons.length-1; i++) buttons[i].disabled = true;

        var lists = data[data.length-2].getElementsByTagName('ul')[0].getElementsByTagName('li');
        var inputs = data[data.length-2].getElementsByTagName('input');

        inputs[0].disabled = false;
        inputs[0].value = id;

        inputs[4].disabled = false;
        inputs[4].value = refundAmount;

        inputs[5].disabled = false;
        inputs[5].value = reason;

        inputs[6].disabled = false;
        inputs[6].value = "refund";

        lists[0].style.display = "block";
        lists[1].style.display = "block";
        lists[2].style.display = "block";

        lists[0].innerHTML = "Refund amount: " + refundAmount;
        lists[1].innerHTML = "Reason: " + reason;
        lists[2].innerHTML = "Action: " + "Will be sent to pending refund table";
    }
}

function rejectSaveClick(){

    var modalBody = document.getElementById('rejectDataModalBody');
    var divs = modalBody.getElementsByTagName('div');

    var id = divs[0].getElementsByTagName('input')[0].value;
    var reason = divs[5].getElementsByTagName('input')[0].value;

    if(reason != ""){

        var row = document.getElementById('row' + id);
        var td = row.getElementsByTagName('td');

        var buttons = td[td.length-1].getElementsByTagName('button');
        for(var j=0; j<buttons.length-1; j++) buttons[j].disabled = true;

        var col = td[td.length - 2];
        var inputs = col.getElementsByTagName('input');
        var list = col.getElementsByTagName('ul')[0].getElementsByTagName('li');

        list[0].innerHTML = "Reason: " + reason;
        list[0].style.display = "block";

        list[1].innerHTML = "Action: " + "Will be rejected";
        list[1].style.display = "block";

        inputs[0].value = id;
        inputs[0].disabled = false;

        inputs[5].value = reason;
        inputs[5].disabled = false;

        inputs[6].value = "reject";
        inputs[6].disabled = false;
    }
}