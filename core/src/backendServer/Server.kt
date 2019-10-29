package backendServer

import com.badlogic.gdx.Gdx
import io.socket.client.IO
import io.socket.client.Socket

class Server {
    private lateinit var socket: Socket

    fun configSocketEvents() {
        socket.on(Socket.EVENT_CONNECT) {
            Gdx.app.log("SocketIO", "Connected")
        }
                .on("socketID") { data ->
                    Gdx.app.log("SocketIO", "Connected ${data[0]}")
                }
    }

    fun connectionSocket() {
        try {
            socket = IO.socket("http://localhost:8080");
            socket.connect();
        }catch (e: Exception){}
    }
}