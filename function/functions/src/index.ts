import * as functions from 'firebase-functions';                // The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
import * as admin from 'firebase-admin';                        // The Firebase Admin SDK to access the Firebase Firestore Database.

admin.initializeApp(functions.config().firebase);               //this will initialize the firebase app 
//here notification is the collection id
//{notification_id} is the Id of Each notification
exports.sendRequestNotification = functions.database.ref('/TVAC/Notification/{TargetUserId}/{NotificationId}/')
    .onWrite(async (snapshot, context) => {
        //This is anonymous handler function


        //targetId is targetUserId whom you have sent a friend request
        const targetId = context.params.TargetUserId;

        if (!snapshot.after.val()) {
            //This means that someone has deleted the notification node. So need to do nothing
            console.log("Data Deleted!");
            return;
        }



        /*
        Database structure
        TVAC
            Notifications
                {tagetUid}
                    {notificationId}
                        from: {senderUid}
                        type: "request"


        */

        //Right now we can say that snapshot contains targetUid and all the rest of 
        //the json object

        //Getting the sendUserId which can be retrived by the snapshot 
        //we got through onWrite function call
        const fromUserId = snapshot.after.child("from").val();

        //Getting the sendUserId which can be retrived by the snapshot 
        //we got through onWrite function call
        const requestType = snapshot.after.child("type").val();

        //Writing the sender receiver and request type to console
        console.log("Target ID: " + targetId
            + "\nFrom UserId: " + fromUserId
            + "\nType: " + requestType);

        //creating target username reference
        // var targetUserNameReference = db.ref("TVAC/Users/" + targetId + "/name");
        const targetProfileReference = admin.database().ref(`TVAC/Users/${targetId}`).once("value");


        //creating sender username reference
        // var senderUserNameReference = db.ref("TVAC/Users/" + fromUserId + "/name");
        const senderProfileReference = admin.database().ref(`TVAC/Users/${fromUserId}`).once("value");



        return senderProfileReference.then(result => {
            const senderName = result.child("name").val();

            targetProfileReference.then(result2 => {
                const receiverName = result2.child("name").val();
                const receiverDeviceToken = result2.child("tokenId").val();

                console.log(senderName + " want to add " + receiverName);

                const payload = {
                    // notification: {
                    //     title: `New Friend Request`,
                    //     body: `${senderName} sent you requst!`,
                    //     click_action: `com.androidbull.firebasechatapp_TARGET_NOTIFICATION`
                    // }, 
                    data: {
                        title: `New Friend Request`,
                        body: `${senderName} sent you requst!`,
                        click_action: `com.androidbull.firebasechatapp_TARGET_NOTIFICATION`,
                        from_user_id:fromUserId
                    }
                }

                admin.messaging().sendToDevice(receiverDeviceToken, payload).then((res: any) => {
                    console.log("Notification sent")
                }).catch(notiError =>{console.log("Error: "+notiError)});



            })
                .catch(err => { console.log("Error: " + err) })
                ;
        });

        // let senderProfile;
        // let targetProfile;

        // const result = await Promise.all([senderProfileReference, targetProfileReference]);
        // senderProfile = result[0];
        // targetProfile = result[1];


        // // console.log('Target profile token ID: ',targetProfile);
        // console.log(`${senderProfile.name} want to sent requst to ${targetProfile.name}`)
        // return admin.messaging().sendToDevice(`${targetProfile.tokenId}`, payload).then((res: any) => {
        //     console.log(`${senderProfile.name} sent requst to ${targetProfile.name}`)
        // })

        // //creating receiver tokenId reference
        // var targetTokenIdReference = db.ref("TVAC/Users/" + targetId + "/tokenId");

        // // Attach an asynchronous callback to read the data at our posts reference
        // targetUserNameReference.on("value", function (newData) {
        //     //we got the target username
        //     //storing it and then going to sender username
        //     const targetUserName = newData.val();

        //     console.log("Target Username: " + targetUserName);
        //     senderUserNameReference.on("value", function (newnewData) {
        //         //we got the sender username
        //         const senderUserName = newnewData.val();

        //         const payload = {
        //             notification: {
        //                 title: `${senderUserName} sent you a friend request`,
        //                 body: "This body",
        //             },
        //             data: {
        //                 data1: `${senderUserName} sent you a friend request`,
        //                 data2: `Check new friend request`
        //             }
        //         }

        //         console.log("Sender Username: " + senderUserName);

        //         //Getting target device token id so that notification could be sent
        //         //to that particular device only
        //         targetTokenIdReference.on("value", function (notificationData) {
        //             const targetTokenID = notificationData.val();
        //             console.log("Target token id: " + targetTokenID)
        //             return admin.messaging().sendToDevice(targetTokenID, payload).then((res: any) => {
        //                 console.log(`${senderUserName} sent ${requestType} to ${targetUserName}
        //                         \nNotification Delivered at device id: ${targetTokenID}
        //                         \n\nResponse: ${res.details}`)

        //                 //Priting the result
        //                 const error = res.error;
        //                 if (error) {
        //                     console.log("Failed sending notification to" + res.error)
        //                     // Cleanup the tokens who are not registered anymore.
        //                     if (error.code === 'messaging/invalid-registration-token') {
        //                         console.log("messsaging/invalid-registration-token")
        //                     }else if(error.code === 'messaging/registration-token-not-registered'){
        //                         console.log("messaging/registration-token-not-registered")
        //                     }
        //                 }else {
        //                     console.log("No Error")
        //                 }



        //             }).catch((err: string) => {
        //                 console.log('something went wrong ' + err)
        //             })


        //         }, function (errorObject) {
        //             //could not read target token ID
        //             console.log("This read failed: " + errorObject.code);
        //         });



        //     }, function (errorObject) {
        //         //could not read sender user name
        //         console.log("This read failed: " + errorObject.code)
        //     });


        // }, function (errorObject) {
        //     //could not read sender user name
        //     console.log("The read failed: " + errorObject.code);
        // });

    })
