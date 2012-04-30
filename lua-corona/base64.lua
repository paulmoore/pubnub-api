require "bit"

base64 = {}

local ENCODABET = {
	'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
	'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
	'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
	'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
	'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
	'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
	'8', '9', '-', '_'
}

local DECODABET = {
	62,  0,  0, 52, 53, 54, 55, 56, 57, 58,
	59, 60, 61,  0,  0,  0,  0,  0,  0,  0,
	 0,  1,  2,  3,  4,  5,  6,  7,  8,  9,
	10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
	20, 21, 22, 23, 24, 25,  0,  0,  0,  0,
	63,  0, 26, 27, 28, 29, 30, 31, 32, 33,
	34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
	44, 45, 46, 47, 48, 49, 50, 51
}

function base64.encode (input)
	
	local bytes = { input:byte(i, input:len()) }

	local out = {}
	
	-- Go through each triplet of 3 bytes, which produce 4 octets.
	local i = 1
	while i <= #bytes - 2 do
		local buffer = 0
		
		-- Fill the buffer with the bytes, producing a 24-bit integer.
		local b = bit.blshift(bytes[i], 16)
		b = bit.band(b, 0xff0000)
		buffer = bit.bor(buffer, b)
		
		b = bit.blshift(bytes[i + 1], 8)
		b = bit.band(b, 0xff00)
		buffer = bit.bor(buffer, b)
		
		b = bit.band(bytes[i + 2], 0xff)
		buffer = bit.bor(buffer, b)
		
		-- Read out the 4 octets into the output buffer.
		b = bit.blogic_rshift(buffer, 18)
		b = bit.band(b, 0x3f)
		out[#out + 1] = ENCODABET[b + 1]
		
		b = bit.blogic_rshift(buffer, 12)
		b = bit.band(b, 0x3f)
		out[#out + 1] = ENCODABET[b + 1]
		
		b = bit.blogic_rshift(buffer, 6)
		b = bit.band(b, 0x3f)
		out[#out + 1] = ENCODABET[b + 1]
		
		b = bit.band(buffer, 0x3f)
		out[#out + 1] = ENCODABET[b + 1]
				
		i = i + 3
	end
	
	-- Special case 1: One byte extra, will produce 2 octets.
	if #bytes % 3 == 1 then
		local buffer = bit.blshift(bytes[i], 16)
		buffer = bit.band(buffer, 0xff0000)
		
		local b = bit.blogic_rshift(buffer, 18)
		b = bit.band(b, 0x3f)
		out[#out + 1] = ENCODABET[b + 1]
		
		b = bit.blogic_rshift(buffer, 12)
		b = bit.band(b, 0x3f)
		out[#out + 1] = ENCODABET[b + 1]
		
	-- Special case 2: Two bytes extra, will produce 3 octets.
	elseif #bytes % 3 == 2 then
		local buffer = 0
		
		local b = bit.blshift(bytes[i], 16)
		b = bit.band(b, 0xff0000)
		buffer = bit.bor(buffer, b)
		
		b = bit.blshift(bytes[i + 1], 8)
		b = bit.band(b, 0xff00)
		buffer = bit.bor(buffer, b)

		b = bit.blogic_rshift(buffer, 18)
		b = bit.band(b, 0x3f)
		out[#out + 1] = ENCODABET[b + 1]
		
		b = bit.blogic_rshift(buffer, 12)
		b = bit.band(b, 0x3f)
		out[#out + 1] = ENCODABET[b + 1]
		
		b = bit.blogic_rshift(buffer, 6)
		b = bit.band(b, 0x3f)
		out[#out + 1] = ENCODABET[b + 1]
	end
	
	return table.concat(out)
	
end

function base64.decode (input)
	
	local out = {}
	
	-- Go through each group of 4 octets to obtain 3 bytes.
	local i = 1
	while i <= input:len() - 3 do
		local buffer = 0
		
		-- Read the 4 octets into the buffer, producing a 24-bit integer.
		local b = input:byte(i)
		b = DECODABET[b - 44]
		b = bit.blshift(b, 18)
		buffer = bit.bor(buffer, b)
		i = i + 1
		
		b = input:byte(i)
		b = DECODABET[b - 44]
		b = bit.blshift(b, 12)
		buffer = bit.bor(buffer, b)
		i = i + 1
		
		b = input:byte(i)
		b = DECODABET[b - 44]
		b = bit.blshift(b, 6)
		buffer = bit.bor(buffer, b)
		i = i + 1
		
		b = input:byte(i)
		b = DECODABET[b - 44]
		buffer = bit.bor(buffer, b)
		i = i + 1
		
		-- Append the 3 re-constructed bytes into the output buffer.
		b = bit.blogic_rshift(buffer, 16)
		b = bit.band(b, 0xff)
		out[#out + 1] = b
		
		b = bit.blogic_rshift(buffer, 8)
		b = bit.band(b, 0xff)
		out[#out + 1] = b
		
		b = bit.band(buffer, 0xff)
		out[#out + 1] = b
	end

	-- Special case 1: Only 2 octets remain, producing 1 byte.
	if input:len() % 4 == 2 then
		local buffer = 0

		local b = input:byte(i)
		b = DECODABET[b - 44]
		b = bit.blshift(b, 18)
		buffer = bit.bor(buffer, b)
		i = i + 1
		
		b = input:byte(i)
		b = DECODABET[b - 44]
		b = bit.blshift(b, 12)
		buffer = bit.bor(buffer, b)
		i = i + 1
		
		b = bit.blogic_rshift(buffer, 16)
		b = bit.band(b, 0xff)
		out[#out + 1] = b
		
	-- Special case 2: Only 3 octets remain, producing 2 bytes.
	elseif input:len() % 4 == 3 then
		local buffer = 0
		
		local b = input:byte(i)
		b = DECODABET[b - 44]
		b = bit.blshift(b, 18)
		buffer = bit.bor(buffer, b)
		i = i + 1
		
		b = input:byte(i)
		b = DECODABET[b - 44]
		b = bit.blshift(b, 12)
		buffer = bit.bor(buffer, b)
		i = i + 1
		
		b = input:byte(i)
		b = DECODABET[b - 44]
		b = bit.blshift(b, 6)
		buffer = bit.bor(buffer, b)
		i = i + 1

		b = bit.blogic_rshift(buffer, 16)
		b = bit.band(b, 0xff)
		out[#out + 1] = b
		
		b = bit.blogic_rshift(buffer, 8)
		b = bit.band(b, 0xff)
		out[#out + 1] = b
	end

	return string.char(unpack(out))
	
end
