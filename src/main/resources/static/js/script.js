console.log("This is javascript");

const toggleSideBar=()=>{
if($(".sideBar").is(":visible")) {
    $(".sideBar").css("display","none");
    $(".content").css("margin-left","1%");
}else {
    $(".sideBar").css("display","block");
    $(".content").css("margin-left","20%");
}
};

const search=()=>{
//console.log("searching...");

let query=$("#search-input").val();
console.log(query);

if(query==''){
$(".search-result").hide();
}else{
console.log(query);

// sending request to server
let url=`http://localhost:8080/search/${query}`

fetch(url).then(response=>{
return response.json();
}).then((data) => {
//data
console.log(data);

let text=`<div class='list-group'>`

data.forEach((contact) =>{
text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`
});

text+=`</div>`;

$(".search-result").html(text);
$(".search-result").show();

});

//$(".search-result").show();
}
};

const donate = () => {

    let amount=$("#payment_field").val();
    console.log(amount);
    if(amount == '' || amount == null) {

        swal("Warning!", "amount is required","warning");

        return;
    }

    if(amount <= 0) {

        swal("Warning!", "amount is more than 0","warning");

        return;
    }

    // we will use ajax to send request to server to create order- jquery

    $.ajax({
    url: "/user/create_order",
    data: JSON.stringify({amount:amount, info: "order_request" }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (response) {
    // invoked where success
    console.log(response);
    if(response.status == "created") {

        // open payment form
        let options = {
        key: "rzp_test_AIWgmWc9Y5bFxH",
        amount: response.amount,
        currency: "INR",
        name: "Smart Contact Manager",
        description: "Donation",
        image: "https://icon2.cleanpng.com/20180328/wvw/kisspng-computer-icons-address-book-google-contacts-book-now-button-5abb94989e1ec5.7608596315222427126477.jpg",
        order_id: response.id,

        handler: function (response) {
            console.log(response.razorpay_payment_id);
            console.log(response.razorpay_order_id);
            console.log(response.razorpay_signature);
            console.log("payment successful");

            updatePaymentOnServer (
            response.razorpay_payment_id,
            response.razorpay_order_id,
            "paid"
            );
        },
         prefill: {
             name: "",
             email: "",
             contact: "",
         },
         notes: {
             address: "Learning languages",
         },
         theme: {
             color: "#3399cc",
         },
        };

        let rzp = new Razorpay(options);

        rzp.on("payment.failed", function (response){
                console.log(response.error.code);
                console.log(response.error.description);
                console.log(response.error.source);
                console.log(response.error.step);
                console.log(response.error.reason);
                console.log(response.error.metadata.order_id);
                console.log(response.error.metadata.payment_id);

                swal("Failed!", "Oops payment failed !!","danger");

        });

        rzp.open();
    }
    },
    error: function (error) {
    // invoked when error
    console.log(error);
                swal("Failed!", "Something went wrong !!","danger");
    },
    });
};

function updatePaymentOnServer (payment_id, order_id, status)
{
 $.ajax({
    url: "/user/update_order",
    data: JSON.stringify({payment_id:payment_id, order_id:order_id, status:status}),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (response) {
       swal("Good jobs", "payment successfully !!", "success");
    },
    error: function (error) {
       swal("Failed!", "Your payment is success, but we did not get on server, we will contact you as soon as possible on gmail !!","danger");
    }
    })
}