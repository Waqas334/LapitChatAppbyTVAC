import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp(functions.config().firebase);
exports.friendRequestAccetionNotifier  = functions.database.ref("/TVAC/Notification/{TargetUserId}/{NotificationId}/")
.onDelete(async (snapshot,context)=>{

    const targetUserId = context.params.TargetUserId;
    const acceptorUsaerId = snapshot
    

})