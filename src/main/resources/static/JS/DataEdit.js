
window.onload = function() {

    var forceLogoutButton = document.getElementsByClassName('ForceLogout')[0];
    forceLogoutButton.onclick = function(){

        var input = document.getElementById('forceLogoutInput');
        input.value = 1;
    }

    var OtherDataEdit = document.getElementsByClassName('OtherTableEdit')[0];
    OtherDataEdit.onclick = function(){

        var row = OtherDataEdit.parentElement.parentElement;
        var data = row.getElementsByTagName('td');

        var modalBody = document.getElementById('otherTableModalBody');
        var divs = modalBody.getElementsByTagName('div');
        var inputs = [];
        for(var i=0; i<divs.length; i++) inputs.push(divs[i].getElementsByTagName('input')[0]);

        for(var i=0; i<inputs.length; i++) inputs[i].value = data[i].innerText;

    };

    var OtherDataEditCancel = document.getElementsByClassName('OtherTableCancel')[0];
    OtherDataEditCancel.onclick = function(){

        var row = document.getElementById('OtherTableDataRow');
        var data = row.getElementsByTagName('td');
        var col = data[data.length-2];
        var button = data[data.length-1].getElementsByTagName('button')[0];

        var lists = col.getElementsByTagName('ul')[0].getElementsByTagName('li');
        var targets = col.getElementsByTagName('input');

        for(var i=0; i<lists.length; i++) {
            lists[i].innerHTML = "";
            lists[i].style.display = "none"
        }

        for(var i=1; i<targets.length; i++){
            targets[i].value = "-1";
            targets[i].disabled = true;
        }
        targets[0].value = "-1";
        button.disabled = false;

        for(var i=0; i<data.length-2; i++){

            var savePrev = data[i].childNodes[1];
            if(savePrev.value == "-1") continue;

            data[i].childNodes[0].nodeValue = savePrev.value;
            savePrev.value = "-1";
        }
    }

    var TransactionTableEdit = document.getElementsByClassName('TransactionTableEdit');
    for(var i=0; i<TransactionTableEdit.length; i++){
        (
            function(object){

                object.onclick = function(){

                    var row = object.parentElement.parentElement;
                    var data = row.getElementsByTagName('td');

                    var title = document.getElementById('transactionTableModalTitle');
                    var id = data[0].innerHTML;
                    title.innerHTML = "Editing transaction number, id: " + id;

                    var modalBody = document.getElementById('transactionTableModalBody');
                    var divs = modalBody.getElementsByTagName('div');
                    var inputs = [];
                    for(var j=0; j<divs.length; j++) inputs.push(divs[j].getElementsByTagName('input')[0]);

                    for(var j=0; j<inputs.length; j++) inputs[j].value = data[j].innerHTML;
                }
            }
        )(TransactionTableEdit[i]);
    }

    var TransactionTableCancel = document.getElementsByClassName('TransactionTableCancel');
    for(var i=0; i<TransactionTableCancel.length; i++){
        (
            function(object){

                object.onclick = function(){

                    var row = object.parentElement.parentElement;
                    var data = row.getElementsByTagName('td');

                    var button = data[data.length-1].getElementsByTagName('button')[0];
                    button.disabled = false;

                    var col = data[data.length-2];
                    var lists = col.getElementsByTagName('ul')[0].getElementsByTagName('li');
                    var targets = col.getElementsByTagName('input');

                    var count = document.getElementById('transactionTableCount');
                    var dec = false;

                    for(var j=0; j<lists.length; j++){
                        lists[j].innerHTML = "";
                        lists[j].style.display = "none";
                    }

                    for(var j=0; j<targets.length; j++){

                        if(targets[j].value != data[j].innerHTML) dec = true;

                        targets[j].value = data[j].innerHTML;
                        targets[j].disabled = true;
                    }

                    if(dec) count.value = parseInt(count.value) - 1;
                }
            }
        )(TransactionTableCancel[i]);
    }

    var WithdrawEdit = document.getElementsByClassName('WithdrawEdit')[0];
    WithdrawEdit.onclick = function(){

        var row = WithdrawEdit.parentElement.parentElement;
        var col = row.getElementsByTagName('td')[0];

        var modalBody = document.getElementById('withdrawEditModalBody');
        var div = modalBody.getElementsByTagName('div')[0];
        var input = div.getElementsByTagName('input')[0];
        input.value = col.innerText;
    }

    var BuyEdit = document.getElementsByClassName('BuyPackageEdit');
    for(var i=0; i<BuyEdit.length; i++){
        (
            function(object, i){

                object.onclick = function(){


                    var amountRow = document.getElementById('coinAmountRow');
                    var priceRow = document.getElementById('coinPriceRow');

                    var amount = amountRow.getElementsByTagName('td')[i].innerText;
                    var price = priceRow.getElementsByTagName('td')[i].innerText;

                    var modalBody = document.getElementById('buyPackageEditModalBody');
                    var divs = modalBody.getElementsByTagName('div');
                    var inputs = [];
                    for(var j=0; j<divs.length; j++) inputs.push(divs[j].getElementsByTagName('input')[0]);

                    var title = document.getElementById('buyPackageEditModalTitle');
                    title.innerHTML = "Editing buy package, id: " + (parseInt(i) + 1);

                    inputs[0].value = parseInt(i) + 1;
                    inputs[1].value = amount;
                    inputs[2].value = price;
                }
            }
        )(BuyEdit[i], i);
    }

    var WithdrawEditCancel = document.getElementsByClassName('WithdrawEditCancel')[0];
    WithdrawEditCancel.onclick = function(){

        var row = document.getElementById('coinPriceWithdraw');
        var cols = row.getElementsByTagName('td');
        var col1 = row.getElementsByTagName('td')[0];
        var col2 = row.getElementsByTagName('td')[1];
        var col3 = row.getElementsByTagName('td')[2];

        var cnt = document.getElementById('withdrawCount');
        cnt.value = 0;

        var b = col3.getElementsByTagName('button')[0];
        b.disabled = false;

        var list = col2.getElementsByTagName('ul')[0].getElementsByTagName('li')[0];
        list.innerHTML = "";
        list.style.display = "none";

        var savePrev = col1.childNodes[1];
        var cur = savePrev.value;
        savePrev.value = -1;

        var input = col2.getElementsByTagName('input')[0];
        input.value = cur;
        input.disabled = true;

        col1.childNodes[0].nodeValue = cur;
    }

    var BuyEditCancel = document.getElementsByClassName('BuyPackageEditCancel');
    for(var i=0; i<BuyEditCancel.length; i++){
        (
            function(object, i){

                object.onclick = function(){

                    var amountRow = document.getElementById('coinAmountRow');
                    var priceRow = document.getElementById('coinPriceRow');

                    var amountCols = amountRow.getElementsByTagName('td');
                    var priceCols = priceRow.getElementsByTagName('td');

                    var cnt = document.getElementById('buyPackageCount');
                    cnt.value = parseInt(cnt.value) - 1;

                    amountCol = amountCols[i];
                    priceCol = priceCols[i];

                    var savePrevAmount = amountCol.childNodes[1];
                    amountCol.childNodes[0].nodeValue = savePrevAmount.value;
                    var amount = savePrevAmount.value;
                    savePrevAmount.value = -1;

                    var savePrevPrice = priceCol.childNodes[1];
                    priceCol.childNodes[0].nodeValue = savePrevPrice.value;
                    var price = savePrevPrice.value;
                    savePrevPrice.value = -1;

                    var b = document.getElementsByClassName('BuyPackageEdit')[i];
                    b.disabled = false;

                    var dataRow = document.getElementById('coinPriceNewDataRow');
                    var dataCol = dataRow.getElementsByTagName('td')[i];

                    var lists = dataCol.getElementsByTagName('ul')[0].getElementsByTagName('li');
                    var targets = dataCol.getElementsByTagName('input');

                    targets[0].disabled = true;
                    targets[1].disabled = true;
                    targets[2].disabled = true;

                    targets[1].value = amount;
                    targets[2].value = price;

                    for(var j=0; j<lists.length; j++){
                        lists[j].innerHTML = "";
                        lists[j].style.display = "none";
                    }
                }
            }
        )(BuyEditCancel[i], i);
    }

    var BoardDataEdit = document.getElementsByClassName('BoardDataEdit');
    for(var i=0; i<BoardDataEdit.length; i++){
        (
            function(object, i){

                object.onclick = function(){

                    var title = document.getElementById('boardDataEditModalTitle');
                    title.innerHTML = "Editig board, id: " + (i + 1);

                    var modalBody = document.getElementById('boardDataEditModalBody');
                    var divs = modalBody.getElementsByTagName('div');
                    var inputs = [];
                    for(var j=0; j<divs.length; j++) inputs.push(divs[j].getElementsByTagName('input')[0]);

                    var minCallValueRow = document.getElementById('minCallValueRow');
                    var minEntryValueRow = document.getElementById('minEntryValueRow');
                    var maxEntryValueRow = document.getElementById('maxEntryValueRow');
                    var mcrValueRow = document.getElementById('mcrValueRow');

                    var minCallValueCol = minCallValueRow.getElementsByTagName('td')[i];
                    var minEntryValueCol = minEntryValueRow.getElementsByTagName('td')[i];
                    var maxEntryValueCol = maxEntryValueRow.getElementsByTagName('td')[i];
                    var mcrValueCol = mcrValueRow.getElementsByTagName('td')[i];

                    inputs[0].value = (i + 1);
                    inputs[1].value = minCallValueCol.innerText;
                    inputs[2].value = minEntryValueCol.innerText;
                    inputs[3].value = maxEntryValueCol.innerText;
                    inputs[4].value = mcrValueCol.innerText;
                }

            }
        )(BoardDataEdit[i], i);
    }

    var BoardDataEditCancel = document.getElementsByClassName('BoardDataEditCancel');
    for(var i=0; i<BoardDataEditCancel.length; i++){
        (
            function(object, i){

                object.onclick = function(){

                    var minCallValueRow = document.getElementById('minCallValueRow');
                    var minEntryValueRow = document.getElementById('minEntryValueRow');
                    var maxEntryValueRow = document.getElementById('maxEntryValueRow');
                    var mcrValueRow = document.getElementById('mcrValueRow');

                    var id = i;

                    var minCallValueCol = minCallValueRow.getElementsByTagName('td')[id];
                    var minEntryValueCol = minEntryValueRow.getElementsByTagName('td')[id];
                    var maxEntryValueCol = maxEntryValueRow.getElementsByTagName('td')[id];
                    var mcrValueCol = mcrValueRow.getElementsByTagName('td')[id];

                    var dec = false;
                    var curMinCallValue;
                    var curMinEntryValue;
                    var curMaxEntryValue;
                    var curMCRValue;

                    var savePrevMinCallValue = minCallValueCol.childNodes[1];
                    if(savePrevMinCallValue.value != "-1"){
                        curMinCallValue = savePrevMinCallValue.value;
                        savePrevMinCallValue.value = -1;
                        minCallValueCol.childNodes[0].nodeValue = curMinCallValue;
                        dec = true;
                    }
                    else curMinCallValue = minCallValueCol.innerText;

                    var savePrevMinEntryValue = minEntryValueCol.childNodes[1];
                    if(savePrevMinEntryValue.value != "-1"){
                        curMinEntryValue = savePrevMinEntryValue.value;
                        savePrevMinEntryValue.value = -1;
                        minEntryValueCol.childNodes[0].nodeValue = curMinEntryValue;
                        dec = true;
                    }
                    else curMinEntryValue = minEntryValueCol.innerText;

                    var savePrevMaxEntryValue = maxEntryValueCol.childNodes[1];
                    if(savePrevMaxEntryValue.value != "-1"){
                        curMaxEntryValue = savePrevMaxEntryValue.value;
                        savePrevMaxEntryValue.value = -1;
                        maxEntryValueCol.childNodes[0].nodeValue = curMaxEntryValue;
                        dec = true;
                    }
                    else curMaxEntryValue = maxEntryValueCol.innerText;

                    var savePrevMCRValue = mcrValueCol.childNodes[1];
                    if(savePrevMCRValue.value != "-1"){
                        curMCRValue = savePrevMCRValue.value;
                        savePrevMCRValue.value = -1;
                        mcrValueCol.childNodes[0].nodeValue = curMCRValue;
                        dec = true;
                    }
                    else curMCRValue = mcrValueCol.innerText;

                    if(dec == false) return ;

                    var cnt = document.getElementById('boardDataEditCount');
                    cnt.value = parseInt( cnt.value ) - 1;

                    var b = document.getElementsByClassName('BoardDataEdit')[id];
                    b.disabled = false;

                    var NewDataRow = document.getElementById('boardDataNewDataRow');
                    var NewDataCol = NewDataRow.getElementsByTagName('td')[id];
                    var targets = NewDataCol.getElementsByTagName('input');
                    var lists = NewDataCol.getElementsByTagName('ul')[0].getElementsByTagName('li');

                    for(var j=0; j<lists.length; j++){
                        lists[j].innerHTML = "";
                        lists[j].style.display = "none";
                    }

                    targets[1].value = curMinCallValue;
                    targets[2].value = curMinEntryValue;
                    targets[3].value = curMaxEntryValue;
                    targets[4].value = curMCRValue;

                    for(var j=0; j<targets.length; j++) targets[j].disabled = true;

                }
            }
        )(BoardDataEditCancel[i], i);
    }

}

function boardDataSaveClick(){

    var modalBody = document.getElementById('boardDataEditModalBody');
    var divs = modalBody.getElementsByTagName('div');
    var inputs = [];
    for(var j=0; j<divs.length; j++) inputs.push(divs[j].getElementsByTagName('input')[0]);

    var id = parseInt( inputs[0].value ) - 1;
    var minCallValue = inputs[1].value;
    var minEntryValue = inputs[2].value;
    var maxEntryValue = inputs[3].value;
    var mcrValue = inputs[4].value;

    var minCallValueRow = document.getElementById('minCallValueRow');
    var minEntryValueRow = document.getElementById('minEntryValueRow');
    var maxEntryValueRow = document.getElementById('maxEntryValueRow');
    var mcrValueRow = document.getElementById('mcrValueRow');

    var minCallValueCol = minCallValueRow.getElementsByTagName('td')[id];
    var minEntryValueCol = minEntryValueRow.getElementsByTagName('td')[id];
    var maxEntryValueCol = maxEntryValueRow.getElementsByTagName('td')[id];
    var mcrValueCol = mcrValueRow.getElementsByTagName('td')[id];

    var curMinCallValue = minCallValueCol.innerText;
    var curMinEntryValue = minEntryValueCol.innerText;
    var curMaxEntryValue = maxEntryValueCol.innerText;
    var curMCRValue = mcrValueCol.innerText;

    if( ! ( minCallValue != curMinCallValue || minEntryValue != curMinEntryValue ||
            maxEntryValue != curMaxEntryValue || mcrValue != curMCRValue ) ) return ;

    var cnt = document.getElementById('boardDataEditCount');
    cnt.value = parseInt( cnt.value ) + 1;

    var b = document.getElementsByClassName('BoardDataEdit')[id];
    b.disabled = true;

    var savePrevMinCallValue = minCallValueCol.childNodes[1];
    savePrevMinCallValue.value = curMinCallValue;
    minCallValueCol.childNodes[0].nodeValue = minCallValue;

    var savePrevMinEntryValue = minEntryValueCol.childNodes[1];
    savePrevMinEntryValue.value = curMinEntryValue;
    minEntryValueCol.childNodes[0].nodeValue = minEntryValue;

    var savePrevMaxEntryValue = maxEntryValueCol.childNodes[1];
    savePrevMaxEntryValue.value = curMaxEntryValue;
    maxEntryValueCol.childNodes[0].nodeValue = maxEntryValue;

    var savePrevMCRValue = mcrValueCol.childNodes[1];
    savePrevMCRValue.value = curMCRValue;
    mcrValueCol.childNodes[0].nodeValue = mcrValue;

    var NewDataRow = document.getElementById('boardDataNewDataRow');
    var NewDataCol = NewDataRow.getElementsByTagName('td')[id];
    var targets = NewDataCol.getElementsByTagName('input');
    var lists = NewDataCol.getElementsByTagName('ul')[0].getElementsByTagName('li');

    var j = 0;

    if(minCallValue != curMinCallValue){
        lists[j].innerHTML = "Editing minimum call value";
        lists[j].style.display = "block";
        targets[1].value = minCallValue;
        j += 1;
    }
    if(minEntryValue != curMinEntryValue){
        lists[j].innerHTML = "Editing minimum entry value";
        lists[j].style.display = "block";
        targets[2].value = minEntryValue;
        j += 1;
    }
    if(maxEntryValue != curMaxEntryValue){
        lists[j].innerHTML = "Editing maximum entry value";
        lists[j].style.display = "block";
        targets[3].value = maxEntryValue;
        j += 1;
    }
    if(mcrValue != curMCRValue){
        lists[j].innerHTML = "Editing mcr value";
        lists[j].style.display = "block";
        targets[4].value = mcrValue;
        j += 1;
    }

    for(var i=0; i<targets.length; i++) targets[i].disabled = false;
}

function buyPackageSaveClick(){

    var modalBody = document.getElementById('buyPackageEditModalBody');
    var divs = modalBody.getElementsByTagName('div');
    var inputs = [];
    for(var i=0; i<divs.length; i++) inputs.push(divs[i].getElementsByTagName('input')[0]);

    var id = inputs[0].value;
    var amount = inputs[1].value;
    var price = inputs[2].value;

    var amountRow = document.getElementById('coinAmountRow');
    var priceRow = document.getElementById('coinPriceRow');

    var amountCols = amountRow.getElementsByTagName('td');
    var priceCols = priceRow.getElementsByTagName('td');

    var curAmount = amountCols[id-1].innerText;
    var curPrice = priceCols[id-1].innerText;

    if(amount == curAmount && curPrice == price) return ;



    var cnt = document.getElementById('buyPackageCount');
    cnt.value = parseInt(cnt.value) + 1;

    amountCol = amountCols[id-1];
    priceCol = priceCols[id-1];

    var savePrevAmount = amountCol.childNodes[1];
    savePrevAmount.value = curAmount;
    amountCol.childNodes[0].nodeValue = amount;

    var savePrevPrice = priceCol.childNodes[1];
    savePrevPrice.value = curPrice;
    priceCol.childNodes[0].nodeValue = price;

    var b = document.getElementsByClassName('BuyPackageEdit')[id-1];
    b.disabled = true;

    var dataRow = document.getElementById('coinPriceNewDataRow');
    var dataCol = dataRow.getElementsByTagName('td')[id-1];

    var lists = dataCol.getElementsByTagName('ul')[0].getElementsByTagName('li');
    var targets = dataCol.getElementsByTagName('input');

    targets[0].disabled = false;
    targets[1].disabled = false;
    targets[2].disabled = false;

    targets[1].value = amount;
    targets[2].value = price;

    var j = 0;
    if(price != curPrice){
        lists[j].innerHTML = "Editing package price";
        lists[j].style.display = "block";
        j += 1;
    }
    if(amount != curAmount){
        lists[j].innerHTML = "Editing package coin amount";
        lists[j].style.display = "block";
        j += 1;
    }
}

function withdrawEditSaveClick(){

    var modalBody = document.getElementById('withdrawEditModalBody');
    var input = modalBody.getElementsByTagName('div')[0].getElementsByTagName('input')[0];
    var val = parseFloat(input.value);

    if(Number.isNaN(val)) return;

    var row = document.getElementById('coinPriceWithdraw');
    var cols = row.getElementsByTagName('td');
    var col1 = row.getElementsByTagName('td')[0];
    var col2 = row.getElementsByTagName('td')[1];
    var col3 = row.getElementsByTagName('td')[2];

    var cur = col1.childNodes[0].nodeValue;
    if(cur == val) return;

    var cnt = document.getElementById('withdrawCount');
    cnt.value = 1;

    var b = col3.getElementsByTagName('button')[0];
    b.disabled = true;

    var list = col2.getElementsByTagName('ul')[0].getElementsByTagName('li')[0];
    list.innerHTML = "Editing coin price per crore";
    list.style.display = "block";

    var input = col2.getElementsByTagName('input')[0];
    input.value = val;
    input.disabled = false;

    var savePrev = col1.childNodes[1];
    savePrev.value = cur;
    col1.childNodes[0].nodeValue = val;
}

function otherDataSaveClick(){

    var modalBody = document.getElementById('otherTableModalBody');
    var divs = modalBody.getElementsByTagName('div');
    var inputs = [];
    for(var i=0; i<divs.length; i++) inputs.push(divs[i].getElementsByTagName('input')[0]);

    var row = document.getElementById('OtherTableDataRow');
    var data = row.getElementsByTagName('td');
    var col = data[data.length-2];
    var button = data[data.length-1].getElementsByTagName('button')[0];

    var lists = col.getElementsByTagName('ul')[0].getElementsByTagName('li');
    var targets = col.getElementsByTagName('input');

    var j = 0;

    for(var i=0; i<6; i++){
        if(data[i].innerText === inputs[i].value) continue;

        var savePrev = data[i].childNodes[1];
        savePrev.value = data[i].innerText;

        data[i].childNodes[0].nodeValue = inputs[i].value;

        var x = "";

        if(i == 0){
            x += "Editing maximum pending request";
            targets[i+1].value = inputs[i].value;
        }
        else if(i == 1){
            x += "Editing initial coin";
            targets[i+1].value = inputs[i].value;
        }
        else if(i == 2){
            x += "Editing daily video count";
            targets[i+1].value = inputs[i].value;
        }
        else if(i == 3){
            x += "Editing each video coin";
            targets[i+1].value = inputs[i].value;
        }
        else if(i == 4){
            x += "Editing free login coin";
            targets[i+1].value = inputs[i].value;
        }
        else if(i == 5){
            x += "Editing delay after force logout";
            targets[i+1].value = inputs[i].value;
        }

        lists[j].innerHTML = x;
        lists[j].style.display = "block";

        j += 1;

        targets[0].value = 1;
        button.disabled = true;
        inputs[i].value = "";
    }

    if(targets[0].value == 1)
        for(var i=1; i<targets.length; i++) targets[i].disabled = false;


}

function transactionDataSaveClick(){

    var modalBody = document.getElementById('transactionTableModalBody');
    var divs = modalBody.getElementsByTagName('div');
    var inputs = [];
    for(var i=0; i<divs.length; i++) inputs.push(divs[i].getElementsByTagName('input')[0]);

    var id = inputs[0].value;
    var row = document.getElementById('transactionRow' + id);
    var data = row.getElementsByTagName('td');

    var button = data[data.length-1].getElementsByTagName('button')[0];

    var col = data[data.length-2];
    var lists = col.getElementsByTagName('ul')[0].getElementsByTagName('li');
    var targets = col.getElementsByTagName('input');

    var count = document.getElementById('transactionTableCount');
    var inc = false;

    for(var i=0; i<inputs.length; i++){
        if(data[i].innerHTML === inputs[i].value) continue;

        inc = true;

        var x = "";
        if(i === 1) x = "Editing type to " + inputs[i].value;
        else if(i === 2) x = "Editing number to " + inputs[i].value;

        lists[i-1].innerHTML = x;
        lists[i-1].style.display = "block";

        targets[i].value = inputs[i].value;
    }
    if(inc) {
        count.value = parseInt(count.value) + 1;
        button.disabled = true;
        for(var i=0; i<targets.length; i++) targets[i].disabled = false;
    }
}