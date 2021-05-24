

window.onload = function() {

    var refundButtons = document.getElementsByClassName('AddRefund');

    for(var i=0; i<refundButtons.length; i++){

        (
            function(object){

                object.onclick = function(){

                    var row = object.parentElement.parentElement;
                    var columns = row.getElementsByTagName('td');

                    var title = document.getElementById('refundDataModalTitle');
                    title.innerHTML = "Updating refund, id: " + columns[0].innerHTML;
                    var divs = document.getElementById('refundDataModalBody').getElementsByTagName('div');

                    var dataOuts = [];
                    for(var j=0; j<divs.length; j++) dataOuts.push(divs[j].getElementsByTagName('input')[0]);

                    for(var j=0; j<columns.length - 2; j++){
                        var k = j;
                        if(k > 0) k += 2;

                        dataOuts[k].value = columns[j].innerHTML;
                    }
                    dataOuts[1].value = "";
                    dataOuts[2].value = "";
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
                    var button = buttonCol.getElementsByTagName('button')[0];
                    button.disabled = false;

                    var ul = column.getElementsByTagName('ul')[0];
                    ul.style.display = "none";

                    var lists = ul.getElementsByTagName('li');
                    for(var j=0; j<lists.length; j++) {
                        var list = lists[j];
                        list.innerHTML = "";
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


function clickedSave(){

    //on save, authorize modal
    //send to server

    var objId = document.getElementById('id');
    var objTr = document.getElementById('newTransactionId');
    var objSender = document.getElementById('refundSender');

    var id = objId.value;
    var transactionId = objTr.value;
    var sender = objSender.value;

    if(transactionId != "" && sender != "") {

        var row = document.getElementById('row' + id);
        var columns = row.getElementsByTagName('td');

        var targetCol = columns[columns.length-2];
        var buttonCol = columns[columns.length-1];
        var button = buttonCol.getElementsByTagName('button')[0];
        button.disabled = true;

        var inputs = targetCol.getElementsByTagName('input');
        var ul = targetCol.getElementsByTagName('ul')[0];
        var lists = ul.getElementsByTagName('li');

        ul.style.display = "flex";

        lists[0].innerHTML = "Sender: " + sender;
        lists[1].innerHTML = "TransactionId: " + transactionId;

        for(var j=0; j<inputs.length; j++){
            var inp = inputs[j];
            inp.disabled = false;

            if(j == 0) inp.value = id;
            else if(j == 1) inp.value = sender;
            else if(j == 2) inp.value = transactionId;
        }
    }



}