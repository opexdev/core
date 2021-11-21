package co.nilin.opex.matching.engine.core.model

class Pair() {
    lateinit var leftSideName: String
    lateinit var rightSideName: String

    constructor(leftSideName: String, rightSideName: String) : this() {
        this.leftSideName = leftSideName
        this.rightSideName = rightSideName
    }

    override fun toString(): String {
        return "${leftSideName}_$rightSideName"
    }

}