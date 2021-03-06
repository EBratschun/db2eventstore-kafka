package org.event.store.stream

class Event () {
  var id: Long = 0L
  var metadataId: Long = 0L
  var ts: Long = 0L
  var value: Long = 0L

  def setId (id: Long): Unit = {this.id = id}
  def setMetadataId (metadataId: Long): Unit =  {this.metadataId = metadataId}
  def setTimestamp (ts: Long): Unit =  {this.ts = ts}
  def setValue (value: Long): Unit =  {this.value = value}
  def print: Unit = { 
    println(s"""Event is id: ${id}, metatadaId: ${metadataId}, value: ${value}, ts: ${ts}""")
  }
}
