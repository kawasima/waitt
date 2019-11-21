module Model.HeapDump exposing (HeapDump, HeapHistogram, decoder)

import Json.Decode as Decode exposing (Decoder)
import Json.Decode.Pipeline exposing (required, optional)

-- MODEL

type alias HeapDump =
    { heapdump : List HeapHistogram
    }

type alias HeapHistogram =
    { className : String
    , instanceCount : Int
    , totalSize : Int
    }

-- SERIALIZATION

decoderHeapHistogram : Decoder HeapHistogram
decoderHeapHistogram =
    Decode.succeed HeapHistogram
        |> required "className" Decode.string
        |> required "instanceCount" Decode.int
        |> required "totalSize" Decode.int

decoder : Decoder HeapDump
decoder =
    Decode.succeed HeapDump
        |> required "heapdump" (Decode.list decoderHeapHistogram)
