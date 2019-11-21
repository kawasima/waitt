module Model.Server exposing (Server, decoder)

import Json.Decode as Decode exposing (Decoder)
import Json.Decode.Pipeline exposing (required, optional)

-- MODEL

type alias Server =
    { serverMetadata : ServerMetadata
    }

type alias ServerMetadata =
    { name : String
    , status : String
    }

-- SERIALIZATION

decoderServerMetadata : Decoder ServerMetadata
decoderServerMetadata =
    Decode.succeed ServerMetadata
        |> required "name"   Decode.string
        |> required "status" Decode.string

decoder : Decoder Server
decoder =
    Decode.succeed Server
        |> required "serverMetadata" decoderServerMetadata
