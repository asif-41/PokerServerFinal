
window.onload = function() {

    var updateButtons = document.getElementsByClassName('Update');

    for(var i = 0; i<updateButtons.length; i++){

        (
            function(object){
                object.onclick = function(){

                    var inp = object.parentElement.getElementsByClassName('id')[0];
                    var id = inp.value;

                    var inputModal = document.getElementById('update-modal-id');

                    var inp2 = object.parentElement.getElementsByClassName('username')[0];
                    var username = inp2.value;

                    var title = document.getElementById('updateDataTitle');
                    title.innerHTML = "Updating user, id: " + id + " username: " + username;

                    var row = object.parentElement.parentElement;
                    var dataInp = row.getElementsByTagName('td');

                    var modalDivs = document.getElementById('updateDataModalBody').getElementsByTagName('div');
                    var dataOut = [];

                    for(var i=0; i<modalDivs.length; i++) dataOut.push(modalDivs[i].getElementsByTagName('input')[0])
                    for(var i=0; i<dataOut.length; i++) dataOut[i].value = dataInp[i].innerHTML;
                }

            }

        )(updateButtons[i]);
    }


    var deleteButtons = document.getElementsByClassName('Delete');

    for(var i = 0; i<deleteButtons.length; i++){

        (
            function(object){
                object.onclick = function(){

                    var inp = object.parentElement.getElementsByClassName('id')[0];
                    var id = inp.value;

                    var inp2 = object.parentElement.getElementsByClassName('username')[0];
                    var username = inp2.value;

                    var title = document.getElementById('deleteDataTitle');
                    title.innerHTML = "Delete user, id: " + id + " username: " + username;

                    var inputModal = document.getElementById('delete-modal-id');
                    inputModal.value = id;
                }
            }

        )(deleteButtons[i]);
    }


    var banButtons = document.getElementsByClassName('Ban');

    for(var i = 0; i<banButtons.length; i++){

        (
            function(object){
                object.onclick = function(){

                    var inp = object.parentElement.getElementsByClassName('id')[0];
                    var id = inp.value;

                    var inp2 = object.parentElement.getElementsByClassName('username')[0];
                    var username = inp2.value;

                    var title = document.getElementById('banDataTitle');
                    title.innerHTML = "Ban user, id: " + id + " username: " + username;

                    var inputModal = document.getElementById('ban-modal-id');
                    inputModal.value = id;
                }
            }

        )(banButtons[i]);
    }


}


