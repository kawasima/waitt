module Model.Env exposing (Env, KeyValuePair, decoder)

import Json.Decode as Decode exposing (Decoder)
import Json.Decode.Pipeline exposing (required, optional)

-- MODEL

type alias Env =
    { environments : List KeyValuePair
    , properties: List KeyValuePair
    }

type alias KeyValuePair =
    { key : String
    , value : String
    }

-- SERIALIZATION

decoderKeyValuePair : Decoder KeyValuePair
decoderKeyValuePair =
    Decode.succeed KeyValuePair
        |> required "key"   Decode.string
        |> required "value" Decode.string

decoder : Decoder Env
decoder =
    Decode.succeed Env
        |> required "environments" (Decode.list decoderKeyValuePair)
        |> required "properties" (Decode.list decoderKeyValuePair)
