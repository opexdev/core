path "kv/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}

path "secret/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}

path "secret/opex/" {
  capabilities = ["create", "read", "update", "delete", "list"]
}

path "secret/opex-wallet/" {
  capabilities = ["create", "read", "update", "delete", "list"]
}


path "sys/mounts" {
  capabilities = ["create", "read", "update", "delete", "list"]
}

path "sys/auth" {
  capabilities = ["create", "read", "update", "delete", "list"]
}



