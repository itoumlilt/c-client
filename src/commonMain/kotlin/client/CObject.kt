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

package client

import client.utils.ActiveSession
import client.utils.CObjectUId
import client.utils.CService
import client.utils.OperationUId
import crdtlib.crdt.DeltaCRDT

/**
* Class representing a Concordant object.
* @property id Concordant object unique identifier.
* @property readOnly is object openned in read-only mode.
*/
open class CObject<T>(private val id: CObjectUId<T>, private val readOnly: Boolean) {

    /**
    * The encapsulated CRDT.
    */
    protected var crdt: DeltaCRDT<T>? = null

    protected fun beforeUpdate(): OperationUId {
        if (this.readOnly) throw RuntimeException("CObject has been opened in read-only mode.")
        if (ActiveSession == null) RuntimeException("Not active session.")
        this.crdt = CService.getObject<T>(this.id)
        return (ActiveSession as Session).environment.tick()
    }

    protected fun afterUpdate() {
        CService.pushObject<T>(this.id, this.crdt)
    }

    protected fun beforeGetter() {
        this.crdt = CService.getObject<T>(this.id)
    }

    protected fun afterGetter() {
    }

    /**
     * Closes this object.
     */
    fun close() {
    }
}
