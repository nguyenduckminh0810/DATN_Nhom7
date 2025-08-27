//  WEBSOCKET SERVICE CHO FRONTEND
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class WebSocketService {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.notificationCallbacks = [];
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
    }

    //  KẾT NỐI WEBSOCKET
    connect(username, token) {
        return new Promise((resolve, reject) => {
            try {
                //  TẠO SOCKJS CONNECTION
                const socket = new SockJS('http://localhost:8080/ws');

                //  TẠO STOMP CLIENT
                this.stompClient = Stomp.over(socket);

                //  CẤU HÌNH HEADERS VỚI JWT TOKEN
                const headers = {
                    'Authorization': `Bearer ${token}`
                };

                //  CONNECT VỚI HEADERS
                this.stompClient.connect(headers,
                    //  SUCCESS CALLBACK
                    (frame) => {
                        console.log(' WebSocket connected:', frame);
                        this.connected = true;
                        this.reconnectAttempts = 0;

                        //  SUBSCRIBE TO USER-SPECIFIC NOTIFICATIONS
                        this.subscribeToNotifications(username);

                        resolve(frame);
                    },
                    //  ERROR CALLBACK
                    (error) => {
                        console.error(' WebSocket connection error:', error);
                        this.connected = false;
                        this.handleReconnect(username, token);
                        reject(error);
                    }
                );

            } catch (error) {
                console.error(' Error creating WebSocket connection:', error);
                reject(error);
            }
        });
    }

    //  SUBSCRIBE TO NOTIFICATIONS
    subscribeToNotifications(username) {
        if (!this.stompClient || !this.connected) {
            console.error(' WebSocket not connected');
            return;
        }

        try {
            const subscriptionPath = `/user/${username}/queue/notifications`;
            console.log('🔌 Subscribing to:', subscriptionPath);

            //  SUBSCRIBE TO USER-SPECIFIC QUEUE
            this.stompClient.subscribe(subscriptionPath, (message) => {
                try {
                    console.log(' Raw message received:', message);
                    const notification = JSON.parse(message.body);
                    console.log(' Parsed notification:', notification);

                    //  GỌI TẤT CẢ CALLBACKS
                    this.notificationCallbacks.forEach(callback => {
                        try {
                            callback(notification);
                        } catch (error) {
                            console.error(' Error in notification callback:', error);
                        }
                    });

                } catch (error) {
                    console.error(' Error parsing notification:', error);
                    console.error(' Message body:', message.body);
                }
            });

            console.log(' Subscribed to notifications for user:', username);

        } catch (error) {
            console.error(' Error subscribing to notifications:', error);
        }
    }

    //  XỬ LÝ RECONNECT
    handleReconnect(username, token) {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error(' Max reconnection attempts reached');
            return;
        }

        this.reconnectAttempts++;
        console.log(`🔄 Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

        setTimeout(() => {
            this.connect(username, token).catch(error => {
                console.error(' Reconnection failed:', error);
            });
        }, 2000 * this.reconnectAttempts); //  EXPONENTIAL BACKOFF
    }

    //  DISCONNECT
    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect(() => {
                console.log(' WebSocket disconnected');
                this.connected = false;
            });
        }
    }

    //  ADD NOTIFICATION CALLBACK
    onNotification(callback) {
        this.notificationCallbacks.push(callback);
    }

    //  REMOVE NOTIFICATION CALLBACK
    removeNotificationCallback(callback) {
        const index = this.notificationCallbacks.indexOf(callback);
        if (index > -1) {
            this.notificationCallbacks.splice(index, 1);
        }
    }

    //  CHECK CONNECTION STATUS
    isConnected() {
        return this.connected;
    }

    //  SEND MESSAGE (CHO FUTURE USE)
    sendMessage(destination, message) {
        if (!this.stompClient || !this.connected) {
            console.error(' WebSocket not connected');
            return;
        }

        try {
            this.stompClient.send(destination, {}, JSON.stringify(message));
            console.log(' Message sent to:', destination);
        } catch (error) {
            console.error(' Error sending message:', error);
        }
    }
}

//  EXPORT SINGLETON INSTANCE
const websocketService = new WebSocketService();
export default websocketService; 