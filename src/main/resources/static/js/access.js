/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/javascript.js to edit this template
 */

    
function cargar_contenido(id){
    $("#content-page").css("overflow-y","hidden");

    var fadeout=$("#content-page").hide().delay(150).promise();

    $.post({
        url: '/main/content-page/',
        async:true,
        data: {id:id},
        success: function(response) {

            if(response.indexOf('Login') !== -1)
                window.location.href="/auth/login?logout=true";

          fadeout.then(function(){
                var fadein=$("#content-page").html(response).fadeIn(200).promise();

                fadein.then(function(){
                    $("#content-page").css("overflow-y","hidden");

                }); 
            });
        },
        error: function(xhr, status, error) {
          // Maneja cualquier error que ocurra durante la llamada
          fadeout.then(function(){

                var fadein=$("#content-page").html(xhr.responseText).fadeIn(200).promise();

                fadein.then(function(){

                    $("#content-page").css("overflow-y","hidden");
                }); 
            });
        }
    });
}
    
            
