module Model.Application exposing (Application, decoder)

import Json.Decode as Decode exposing (Decoder)
import Json.Decode.Pipeline exposing (required, optional)

-- MODEL

type alias Application =
    { adminAvailable : Bool
    , configuration : Configuration
    }

type alias Configuration =
    { applicationName : String
    , baseDirectory : Directory
    , sourceDirectory : Directory
    , packages : List String
    }

type alias Directory =
    { path : String
    }

-- SERIALIZATION

decoderDirectory : Decoder Directory
decoderDirectory =
    Decode.succeed Directory
        |> required "path" Decode.string

decoderConfiguration : Decoder Configuration
decoderConfiguration =
    Decode.succeed Configuration
        |> required "applicationName" Decode.string
        |> required "baseDirectory" decoderDirectory
        |> required "sourceDirectory" decoderDirectory
        |> required "packages" (Decode.list Decode.string)

decoder : Decoder Application
decoder =
    Decode.succeed Application
        |> required "adminAvailable" Decode.bool
        |> required "configuration"  decoderConfiguration
