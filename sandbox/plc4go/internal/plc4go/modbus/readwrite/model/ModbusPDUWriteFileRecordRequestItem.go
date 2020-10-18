//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
package model

import (
	"errors"
	"plc4x.apache.org/plc4go-modbus-driver/0.8.0/internal/plc4go/spi"
)

// The data-structure of this message
type ModbusPDUWriteFileRecordRequestItem struct {
	ReferenceType uint8
	FileNumber    uint16
	RecordNumber  uint16
	RecordData    []int8
}

// The corresponding interface
type IModbusPDUWriteFileRecordRequestItem interface {
	spi.Message
	Serialize(io spi.WriteBuffer) error
}

func NewModbusPDUWriteFileRecordRequestItem(referenceType uint8, fileNumber uint16, recordNumber uint16, recordData []int8) spi.Message {
	return &ModbusPDUWriteFileRecordRequestItem{ReferenceType: referenceType, FileNumber: fileNumber, RecordNumber: recordNumber, RecordData: recordData}
}

func CastIModbusPDUWriteFileRecordRequestItem(structType interface{}) IModbusPDUWriteFileRecordRequestItem {
	castFunc := func(typ interface{}) IModbusPDUWriteFileRecordRequestItem {
		if iModbusPDUWriteFileRecordRequestItem, ok := typ.(IModbusPDUWriteFileRecordRequestItem); ok {
			return iModbusPDUWriteFileRecordRequestItem
		}
		return nil
	}
	return castFunc(structType)
}

func CastModbusPDUWriteFileRecordRequestItem(structType interface{}) ModbusPDUWriteFileRecordRequestItem {
	castFunc := func(typ interface{}) ModbusPDUWriteFileRecordRequestItem {
		if sModbusPDUWriteFileRecordRequestItem, ok := typ.(ModbusPDUWriteFileRecordRequestItem); ok {
			return sModbusPDUWriteFileRecordRequestItem
		}
		return ModbusPDUWriteFileRecordRequestItem{}
	}
	return castFunc(structType)
}

func (m ModbusPDUWriteFileRecordRequestItem) LengthInBits() uint16 {
	var lengthInBits uint16 = 0

	// Simple field (referenceType)
	lengthInBits += 8

	// Simple field (fileNumber)
	lengthInBits += 16

	// Simple field (recordNumber)
	lengthInBits += 16

	// Implicit Field (recordLength)
	lengthInBits += 16

	// Array field
	if len(m.RecordData) > 0 {
		lengthInBits += 8 * uint16(len(m.RecordData))
	}

	return lengthInBits
}

func (m ModbusPDUWriteFileRecordRequestItem) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func ModbusPDUWriteFileRecordRequestItemParse(io *spi.ReadBuffer) (spi.Message, error) {

	// Simple Field (referenceType)
	referenceType, _referenceTypeErr := io.ReadUint8(8)
	if _referenceTypeErr != nil {
		return nil, errors.New("Error parsing 'referenceType' field " + _referenceTypeErr.Error())
	}

	// Simple Field (fileNumber)
	fileNumber, _fileNumberErr := io.ReadUint16(16)
	if _fileNumberErr != nil {
		return nil, errors.New("Error parsing 'fileNumber' field " + _fileNumberErr.Error())
	}

	// Simple Field (recordNumber)
	recordNumber, _recordNumberErr := io.ReadUint16(16)
	if _recordNumberErr != nil {
		return nil, errors.New("Error parsing 'recordNumber' field " + _recordNumberErr.Error())
	}

	// Implicit Field (recordLength) (Used for parsing, but it's value is not stored as it's implicitly given by the objects content)
	recordLength, _recordLengthErr := io.ReadUint16(16)
	if _recordLengthErr != nil {
		return nil, errors.New("Error parsing 'recordLength' field " + _recordLengthErr.Error())
	}

	// Array field (recordData)
	// Length array
	recordData := make([]int8, 0)
	_recordDataLength := uint16(recordLength) * uint16(uint16(2))
	_recordDataEndPos := io.GetPos() + uint16(_recordDataLength)
	for io.GetPos() < _recordDataEndPos {
		_item, _err := io.ReadInt8(8)
		if _err != nil {
			return nil, errors.New("Error parsing 'recordData' field " + _err.Error())
		}
		recordData = append(recordData, _item)
	}

	// Create the instance
	return NewModbusPDUWriteFileRecordRequestItem(referenceType, fileNumber, recordNumber, recordData), nil
}

func (m ModbusPDUWriteFileRecordRequestItem) Serialize(io spi.WriteBuffer) error {

	// Simple Field (referenceType)
	referenceType := uint8(m.ReferenceType)
	_referenceTypeErr := io.WriteUint8(8, referenceType)
	if _referenceTypeErr != nil {
		return errors.New("Error serializing 'referenceType' field " + _referenceTypeErr.Error())
	}

	// Simple Field (fileNumber)
	fileNumber := uint16(m.FileNumber)
	_fileNumberErr := io.WriteUint16(16, fileNumber)
	if _fileNumberErr != nil {
		return errors.New("Error serializing 'fileNumber' field " + _fileNumberErr.Error())
	}

	// Simple Field (recordNumber)
	recordNumber := uint16(m.RecordNumber)
	_recordNumberErr := io.WriteUint16(16, recordNumber)
	if _recordNumberErr != nil {
		return errors.New("Error serializing 'recordNumber' field " + _recordNumberErr.Error())
	}

	// Implicit Field (recordLength) (Used for parsing, but it's value is not stored as it's implicitly given by the objects content)
	recordLength := uint16(uint16(uint16(len(m.RecordData))) / uint16(uint16(2)))
	_recordLengthErr := io.WriteUint16(16, recordLength)
	if _recordLengthErr != nil {
		return errors.New("Error serializing 'recordLength' field " + _recordLengthErr.Error())
	}

	// Array Field (recordData)
	if m.RecordData != nil {
		for _, _element := range m.RecordData {
			_elementErr := io.WriteInt8(8, _element)
			if _elementErr != nil {
				return errors.New("Error serializing 'recordData' field " + _elementErr.Error())
			}
		}
	}

	return nil
}
