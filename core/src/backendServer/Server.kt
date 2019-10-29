package backendServer

import com.badlogic.gdx.Gdx
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class Server {
    companion object {
        private lateinit var socket: Socket

        fun configSocketEvents() {
            socket.on(Socket.EVENT_CONNECT) {
                Gdx.app.log("SocketIO", "Connected")
            }
            .on("socketID") { data ->
                val obj: JSONObject = data[0] as JSONObject
                val playerId = obj.getString("id")

                Gdx.app.log("SocketIO", "My ID: $playerId")
            }
            .on("newPlayer") { data ->
                val obj: JSONObject = data[0] as JSONObject
                val playerId = obj.getString("id")
                Gdx.app.log("SocketIO", "New player has just connected with ID: $playerId")
            }
        }

        fun connectionSocket() {
            try {
                socket = IO.socket("http://localhost:8080");
                socket.connect();
            } catch (e: Exception) {
            }
        }
    }
}