/*
* Copyright © 2020, Concordant and contributors.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
* associated documentation files (the "Software"), to deal in the Software without restriction,
* including without limitation the rights to use, copy, modify, merge, publish, distribute,
* sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or
* substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
* NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package client.utils

import crdtlib.crdt.DeltaCRDT
import crdtlib.utils.Environment
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 *
 */
class CServiceAdapter {
    companion object {
        /**
         * Connection to the database
         * @param dbName database name
         */
        suspend fun connect(dbName: String): Boolean {
            val client = HttpClient()
            val resp = client.post<String> {
                url("http://127.0.0.1:4000/api/create-app")
                contentType(ContentType.Application.Json)
                body = """{"appName":"$dbName"}"""
            }
            client.close()
            return resp == "\"OK\""

        }

        /**
         * Get a CRDT from the database
         * @param dbName database name
         * @param objectUId crdt id
         */
        suspend fun getObject(dbName: String, objectUId: CObjectUId, env: Environment): DeltaCRDT {
            val client = HttpClient()
            val crdtJson = client.post<String>{
                url("http://127.0.0.1:4000/api/get-object")
                contentType(ContentType.Application.Json)
                body = """{"appName":"$dbName","id":"${Json.encodeToString(objectUId).replace("\"","\\\"")}"}"""
            }
            client.close()
            return DeltaCRDT.fromJson(crdtJson.removePrefix("\"").removeSuffix("\"").replace("""\\"""","""\""""), env)
        }

        /**
         * Get all objects of the database
         * @param dbName database name
         */
        suspend fun getObjects(dbName: String): String {
            val client = HttpClient()
            val resp = client.post<String> {
                url("http://127.0.0.1:4000/api/get-objects")
                contentType(ContentType.Application.Json)
                body = """{"appName":"$dbName"}"""
            }
            client.close()
            return resp
        }

        /**
         * Update a CRDT
         * @param dbName database name
         * @param objectUId CRDT id
         * @param crdt new crdt
         */
        suspend fun updateObject(dbName: String, objectUId: CObjectUId, crdt: DeltaCRDT): Boolean{
            val client = HttpClient()
            val crdtJson = crdt.toJson().replace("\"","\\\"")
            val resp = client.post<String>{
                url("http://127.0.0.1:4000/api/update-object")
                contentType(ContentType.Application.Json)
                body = """{"appName":"$dbName","id":"${Json.encodeToString(objectUId).replace("\"","\\\"")}", "document":"$crdtJson"}"""
            }
            client.close()
            return resp == "\"OK\""
        }

        /**
         * Close the connection to the database
         * @param dbName database name
         */
        fun close(dbName: String): Boolean{
            return true
        }

        /**
         * Delete the database
         * @param dbName database name
         */
        suspend fun delete(dbName: String): Boolean{
            val client = HttpClient()
            val resp = client.post<String> {
                url("http://127.0.0.1:4000/api/delete-app")
                contentType(ContentType.Application.Json)
                body = """{"appName":"$dbName"}"""
            }
            client.close()
            return resp == "\"OK\""
        }
    }
}