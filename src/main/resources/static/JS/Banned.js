
window.onload = function() {

    var permitButtons = document.getElementsByClassName('Permit');

    for(var i = 0; i<permitButtons.length; i++){

        (
            function(object){
                object.onclick = function(){

                    var inp = object.parentElement.getElementsByClassName('id')[0];
                    var id = inp.value;

                    var inp2 = object.parentElement.getElementsByClassName('username')[0];
                    var username = inp2.value;

                    var title = document.getElementById('permitDataTitle');
                    title.innerHTML = "Permit user, id: " + id + " username: " + username;

                    var inputModal = document.getElementById('permit-modal-id');
                    inputModal.value = id;
                }
            }

        )(permitButtons[i]);
    }

}


