module Model.Application exposing (Application, Feature, decoder)

import Json.Decode as Decode exposing (Decoder)
import Json.Decode.Pipeline exposing (required, optional)

-- MODEL

type alias Application =
    { applicationName : String
    , baseDirectory : String
    , sourceDirectory : String
    , packages : List String
    , features : List Feature
    }

type alias Feature =
    { groupId : String
    , artifactId : String
    , version: String
    , packaging : Maybe String
    }
-- SERIALIZATION

decoderFeature : Decoder Feature
decoderFeature =
    Decode.succeed Feature
        |> required "groupId" Decode.string
        |> required "artifactId" Decode.string
        |> required "version" Decode.string
        |> required "type" (Decode.nullable Decode.string)

decoder : Decoder Application
decoder =
    Decode.succeed Application
        |> required "applicationName" Decode.string
        |> required "baseDirectory" Decode.string
        |> required "sourceDirectory" Decode.string
        |> required "packages" (Decode.list Decode.string)
        |> required "features" (Decode.list decoderFeature)
