path "kv/*" {
  capabilities = ["read"]
}

path "secret/*" {
  capabilities = ["read"]
}

path "secret/opex/" {
  capabilities = ["read"]
}

path "secret/opex-wallet/" {
  capabilities = ["read"]
}

path "sys/mounts" {
  capabilities = ["read"]
}

path "sys/auth" {
  capabilities = ["read"]
}



